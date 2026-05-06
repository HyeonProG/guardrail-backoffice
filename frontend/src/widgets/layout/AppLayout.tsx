import { useEffect, useState } from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { logout } from '@/features/auth/api/login';
import { authStorage } from '@/features/auth/model/authStorage';
import { Modal } from '@/shared/ui/Modal';
import { Button } from '@/shared/ui/Button';
import { Header } from '@/widgets/layout/Header';
import { navigationGroups } from '@/widgets/layout/model/navigation';

export function AppLayout() {
  const navigate = useNavigate();
  const [temporaryPasswordModalOpen, setTemporaryPasswordModalOpen] = useState(false);

  useEffect(() => {
    if (authStorage.isTemporaryPassword() && !authStorage.isTemporaryPasswordPromptDismissed()) {
      setTemporaryPasswordModalOpen(true);
    }
  }, []);

  const handleLogout = async () => {
    const sessionId = authStorage.getSessionId();
    if (sessionId) {
      try {
        await logout(sessionId);
      } catch {
        // 로컬 세션 정리는 실패 여부와 무관하게 진행한다.
      }
    }
    authStorage.clear();
    navigate('/login', { replace: true });
  };

  const handleTemporaryPasswordModalClose = () => {
    authStorage.dismissTemporaryPasswordPrompt();
    setTemporaryPasswordModalOpen(false);
  };

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(14,165,233,0.12),_transparent_28%),radial-gradient(circle_at_top_right,_rgba(251,191,36,0.14),_transparent_24%),linear-gradient(180deg,#f8fafc_0%,#eef4f8_100%)] text-ink">
      <Modal
        open={temporaryPasswordModalOpen}
        title="임시 비밀번호 변경 필요"
        description="현재 임시 비밀번호로 로그인되어 있습니다. 보안을 위해 먼저 비밀번호를 변경해 주세요."
        onClose={handleTemporaryPasswordModalClose}
      >
        <div className="space-y-4">
          <p className="text-sm text-slate-600">
            현재 계정은 임시 비밀번호 상태입니다. 내 정보 화면에서 새 비밀번호를 설정한 뒤 계속 사용하는 것을 권장합니다.
          </p>
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={handleTemporaryPasswordModalClose}>
              나중에
            </Button>
            <Link to="/my-account" onClick={handleTemporaryPasswordModalClose}>
              <Button type="button">비밀번호 변경하러 가기</Button>
            </Link>
          </div>
        </div>
      </Modal>
      <Header groups={navigationGroups} onLogout={handleLogout} />
      <main className="mx-auto max-w-[1480px] px-5 py-10 sm:px-6 lg:px-8">
        <div className="animate-fade-up">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
