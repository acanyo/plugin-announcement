export interface Announcement {
  metadata: {
    name: string;
    creationTimestamp: string;
  };
  announcementSpec: {
    title: string;
    type?: string;
    content: string;
    enablePinning: boolean;
    enablePopup: boolean;
    permissions: string;
    position: string;
    autoClose: number;
    closeOnClickOutside: boolean;
    popupInterval: number;
    confettiEnable: boolean;
  };
}

export interface AnnouncementType {
  displayName: string;
  color: string;
}

export interface AnnouncementListResponse {
  items: Announcement[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}
