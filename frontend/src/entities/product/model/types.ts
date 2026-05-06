export type ProductStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'INACTIVE';

export type ProductSelectedOption = {
  productOptionId: string;
  productOptionName: string;
  productOptionItemId: string;
  productOptionItemName: string;
  sortOrder: number;
};

export type Product = {
  id: string;
  categoryId: string;
  name: string;
  description: string;
  selectedOptions: ProductSelectedOption[];
  status: ProductStatus;
  createdAt: string;
  updatedAt: string;
};

export type ProductHistory = {
  id: string;
  productId: string;
  actorId: string;
  actorName: string;
  type: 'CREATED' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'INACTIVATED' | 'UPDATED';
  reason: string | null;
  createdAt: string;
  updatedAt: string;
};
