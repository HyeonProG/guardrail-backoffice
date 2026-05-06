import { Button } from '@/shared/ui/Button';

type PaginationProps = {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
};

function buildPageNumbers(currentPage: number, totalPages: number) {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, index) => index);
  }

  const pages = new Set<number>([0, totalPages - 1, currentPage - 1, currentPage, currentPage + 1]);

  if (currentPage <= 2) {
    pages.add(1);
    pages.add(2);
  }

  if (currentPage >= totalPages - 3) {
    pages.add(totalPages - 2);
    pages.add(totalPages - 3);
  }

  return [...pages].filter((page) => page >= 0 && page < totalPages).sort((a, b) => a - b);
}

export function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) {
    return null;
  }

  const pages = buildPageNumbers(currentPage, totalPages);

  return (
    <div className="flex flex-wrap items-center justify-end gap-2">
      <Button
        type="button"
        variant="secondary"
        disabled={currentPage === 0}
        onClick={() => onPageChange(Math.max(0, currentPage - 1))}
      >
        이전
      </Button>
      {pages.map((page, index) => {
        const previousPage = pages[index - 1];
        const showEllipsis = previousPage !== undefined && page - previousPage > 1;

        return (
          <div key={page} className="flex items-center gap-2">
            {showEllipsis ? <span className="px-1 text-sm text-slate-400">...</span> : null}
            <Button
              type="button"
              variant={page === currentPage ? 'primary' : 'secondary'}
              className="min-w-10 px-3"
              onClick={() => onPageChange(page)}
            >
              {page + 1}
            </Button>
          </div>
        );
      })}
      <Button
        type="button"
        variant="secondary"
        disabled={currentPage + 1 >= totalPages}
        onClick={() => onPageChange(Math.min(totalPages - 1, currentPage + 1))}
      >
        다음
      </Button>
    </div>
  );
}
