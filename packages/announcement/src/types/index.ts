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
    urlPatterns?: string;
    // 弹窗图标配置
    popupIcon?: string;
    popupIconBgColor?: string;
    // 按钮配置
    primaryButtonText?: string;
    primaryButtonColor?: string;
    primaryButtonAction?: string;
    primaryButtonUrl?: string;
    primaryButtonCallback?: string;
    secondaryButtonText?: string;
    secondaryButtonColor?: string;
    secondaryButtonAction?: string;
    secondaryButtonUrl?: string;
    secondaryButtonCallback?: string;
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
