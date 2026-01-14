import type { AttachmentLike } from "@halo-dev/ui-shared";

interface useAttachmentSelectReturn {
  onAttachmentSelect: (attachments: AttachmentLike[]) => void;
  attachmentResult: AttachmentResult;
}

export interface AttachmentResult {
  updateAttachment: (attachments: AttachmentLike[]) => void;
}

export function useAttachmentSelect(): useAttachmentSelectReturn {
  const attachmentResult = {
    updateAttachment: (attachments: AttachmentLike[]) => {
      return attachments;
    },
  };
  const onAttachmentSelect = (attachmentLikes: AttachmentLike[]) => {
    attachmentResult.updateAttachment(attachmentLikes);
  };

  return {
    onAttachmentSelect,
    attachmentResult,
  };
}
