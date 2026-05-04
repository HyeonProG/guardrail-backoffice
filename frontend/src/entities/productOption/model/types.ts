export type ProductOptionStatus = 'ACTIVE' | 'INACTIVE';

export type ProductOptionItem = {
  id: string;
  productOptionId: string;
  name: string;
  additionalPrice: number;
  sortOrder: number;
  status: ProductOptionStatus;
  createdAt: string;
  updatedAt: string;
};

export type ProductOption = {
  id: string;
  categoryId: string;
  name: string;
  sortOrder: number;
  status: ProductOptionStatus;
  createdAt: string;
  updatedAt: string;
  items: ProductOptionItem[];
};
