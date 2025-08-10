import { axiosInstance } from "@halo-dev/api-client";
import {
  AnnouncementV1alpha1Api,
  ApiAnnouncementLikCcV1alpha1AnnouncementApi,
} from "./generated";

// 创建公告API客户端
const announcementV1alpha1Api = new AnnouncementV1alpha1Api(undefined, "", axiosInstance);

const announcementApiClient = new ApiAnnouncementLikCcV1alpha1AnnouncementApi(undefined, "", axiosInstance);

export { announcementV1alpha1Api, announcementApiClient };
