import { Suspense, lazy, type ComponentType, type ReactNode } from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AppLayout } from '@/widgets/layout/AppLayout';
import { ProtectedRoute } from '@/app/routes/ProtectedRoute';

function lazyPage<T extends Record<string, unknown>, K extends keyof T & string>(
  loader: () => Promise<T>,
  exportName: K
) {
  return lazy(async () => {
    const module = await loader();
    return { default: module[exportName] as ComponentType };
  });
}

const LoginPage = lazyPage(() => import('@/pages/login/LoginPage'), 'LoginPage');
const DashboardPage = lazyPage(() => import('@/pages/dashboard/DashboardPage'), 'DashboardPage');
const CategoriesPage = lazyPage(() => import('@/pages/categories/CategoriesPage'), 'CategoriesPage');
const ApprovalRequestsPage = lazyPage(
  () => import('@/pages/approvalRequests/ApprovalRequestsPage'),
  'ApprovalRequestsPage'
);
const ProductApprovalDetailPage = lazyPage(
  () => import('@/pages/approvalRequests/ProductApprovalDetailPage'),
  'ProductApprovalDetailPage'
);
const ProductCreatePage = lazyPage(
  () => import('@/pages/products/ProductCreatePage'),
  'ProductCreatePage'
);
const ProductDetailPage = lazyPage(
  () => import('@/pages/products/ProductDetailPage'),
  'ProductDetailPage'
);
const ProductEditPage = lazyPage(() => import('@/pages/products/ProductEditPage'), 'ProductEditPage');
const ApprovedProductsPage = lazyPage(
  () => import('@/pages/products/ApprovedProductsPage'),
  'ApprovedProductsPage'
);
const ProductsPage = lazyPage(() => import('@/pages/products/ProductsPage'), 'ProductsPage');
const ProductOptionDetailPage = lazyPage(
  () => import('@/pages/productOptions/ProductOptionDetailPage'),
  'ProductOptionDetailPage'
);
const ProductOptionItemDetailPage = lazyPage(
  () => import('@/pages/productOptions/ProductOptionItemDetailPage'),
  'ProductOptionItemDetailPage'
);
const ProductOptionsPage = lazyPage(
  () => import('@/pages/productOptions/ProductOptionsPage'),
  'ProductOptionsPage'
);
const MyAccountPage = lazyPage(() => import('@/pages/myAccount/MyAccountPage'), 'MyAccountPage');
const UsersPage = lazyPage(() => import('@/pages/users/UsersPage'), 'UsersPage');
const NotFoundPage = lazyPage(() => import('@/pages/errors/NotFoundPage'), 'NotFoundPage');
const UnauthorizedPage = lazyPage(
  () => import('@/pages/errors/UnauthorizedPage'),
  'UnauthorizedPage'
);

function withSuspense(element: ReactNode) {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-[40vh] items-center justify-center text-sm font-medium text-slate-500">
          화면을 불러오는 중입니다.
        </div>
      }
    >
      {element}
    </Suspense>
  );
}

export const router = createBrowserRouter([
  {
    path: '/login',
    element: withSuspense(<LoginPage />)
  },
  {
    path: '/unauthorized',
    element: withSuspense(<UnauthorizedPage />)
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <Navigate to="/dashboard" replace />
      },
      {
        path: 'dashboard',
        element: withSuspense(<DashboardPage />)
      },
      {
        path: 'products',
        element: withSuspense(<ProductsPage />)
      },
      {
        path: 'products/approved',
        element: withSuspense(<ApprovedProductsPage />)
      },
      {
        path: 'products/new',
        element: withSuspense(<ProductCreatePage />)
      },
      {
        path: 'approval-requests',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN', 'OPERATOR']}>
            {withSuspense(<ApprovalRequestsPage />)}
          </ProtectedRoute>
        )
      },
      {
        path: 'approval-requests/:productId',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN', 'OPERATOR']}>
            {withSuspense(<ProductApprovalDetailPage />)}
          </ProtectedRoute>
        )
      },
      {
        path: 'products/:productId',
        element: withSuspense(<ProductDetailPage />)
      },
      {
        path: 'products/:productId/edit',
        element: withSuspense(<ProductEditPage />)
      },
      {
        path: 'product-options',
        element: withSuspense(<ProductOptionsPage />)
      },
      {
        path: 'product-options/:productOptionId',
        element: withSuspense(<ProductOptionDetailPage />)
      },
      {
        path: 'product-options/:productOptionId/items/:productOptionItemId',
        element: withSuspense(<ProductOptionItemDetailPage />)
      },
      {
        path: 'categories',
        element: withSuspense(<CategoriesPage />)
      },
      {
        path: 'my-account',
        element: withSuspense(<MyAccountPage />)
      },
      {
        path: 'users',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN', 'OPERATOR']}>
            {withSuspense(<UsersPage />)}
          </ProtectedRoute>
        )
      },
      {
        path: '*',
        element: withSuspense(<NotFoundPage />)
      }
    ]
  },
  {
    path: '*',
    element: withSuspense(<NotFoundPage />)
  }
]);
