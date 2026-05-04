import { createBrowserRouter, Navigate } from 'react-router-dom';
import { LoginPage } from '@/pages/login/LoginPage';
import { DashboardPage } from '@/pages/dashboard/DashboardPage';
import { CategoriesPage } from '@/pages/categories/CategoriesPage';
import { ApprovalRequestsPage } from '@/pages/approvalRequests/ApprovalRequestsPage';
import { ProductApprovalDetailPage } from '@/pages/approvalRequests/ProductApprovalDetailPage';
import { ProductCreatePage } from '@/pages/products/ProductCreatePage';
import { ProductDetailPage } from '@/pages/products/ProductDetailPage';
import { ProductEditPage } from '@/pages/products/ProductEditPage';
import { ApprovedProductsPage } from '@/pages/products/ApprovedProductsPage';
import { ProductsPage } from '@/pages/products/ProductsPage';
import { ProductOptionDetailPage } from '@/pages/productOptions/ProductOptionDetailPage';
import { ProductOptionItemDetailPage } from '@/pages/productOptions/ProductOptionItemDetailPage';
import { ProductOptionsPage } from '@/pages/productOptions/ProductOptionsPage';
import { MyAccountPage } from '@/pages/myAccount/MyAccountPage';
import { UsersPage } from '@/pages/users/UsersPage';
import { NotFoundPage } from '@/pages/errors/NotFoundPage';
import { UnauthorizedPage } from '@/pages/errors/UnauthorizedPage';
import { AppLayout } from '@/widgets/layout/AppLayout';
import { ProtectedRoute } from '@/app/routes/ProtectedRoute';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />
  },
  {
    path: '/unauthorized',
    element: <UnauthorizedPage />
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
        element: <DashboardPage />
      },
      {
        path: 'products',
        element: <ProductsPage />
      },
      {
        path: 'products/approved',
        element: <ApprovedProductsPage />
      },
      {
        path: 'products/new',
        element: <ProductCreatePage />
      },
      {
        path: 'approval-requests',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN', 'OPERATOR']}>
            <ApprovalRequestsPage />
          </ProtectedRoute>
        )
      },
      {
        path: 'approval-requests/:productId',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN', 'OPERATOR']}>
            <ProductApprovalDetailPage />
          </ProtectedRoute>
        )
      },
      {
        path: 'products/:productId',
        element: <ProductDetailPage />
      },
      {
        path: 'products/:productId/edit',
        element: <ProductEditPage />
      },
      {
        path: 'product-options',
        element: <ProductOptionsPage />
      },
      {
        path: 'product-options/:productOptionId',
        element: <ProductOptionDetailPage />
      },
      {
        path: 'product-options/:productOptionId/items/:productOptionItemId',
        element: <ProductOptionItemDetailPage />
      },
      {
        path: 'categories',
        element: <CategoriesPage />
      },
      {
        path: 'my-account',
        element: <MyAccountPage />
      },
      {
        path: 'users',
        element: (
          <ProtectedRoute allowedRoles={['ADMIN', 'OPERATOR']}>
            <UsersPage />
          </ProtectedRoute>
        )
      },
      {
        path: '*',
        element: <NotFoundPage />
      }
    ]
  },
  {
    path: '*',
    element: <NotFoundPage />
  }
]);
