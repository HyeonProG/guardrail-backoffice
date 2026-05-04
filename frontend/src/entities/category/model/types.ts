export type CategoryStatus = 'ACTIVE' | 'INACTIVE';

export type Category = {
  id: string;
  parentId: string | null;
  name: string;
  status: CategoryStatus;
  createdAt: string;
  updatedAt: string;
};
