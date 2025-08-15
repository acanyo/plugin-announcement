<script setup lang="ts">
import {computed, ref, watch, onMounted} from "vue";
import {
  VPageHeader,
  VCard,
  VLoading,
  VEmpty,
  VSpace,
  VButton,
  VPagination,
  IconRefreshLine,
  Dialog,
  Toast,
  VEntityContainer
} from "@halo-dev/components";
import {announcementApiClient, announcementV1alpha1Api} from "@/api";
import AnnouncementListItem from "@/components/AnnouncementListItem.vue";
import IconAnnouncementMegaphone from '~icons/streamline-plump-color/announcement-megaphone?width=1.2em&height=1.2em';
import type {Announcement, AnnouncementList} from "@/api/generated";
import { useRouteQuery } from "@vueuse/router";
import { useQuery } from "@tanstack/vue-query";

const checkAll = ref(false);
const selectedAnnouncement = ref<Announcement>();
const selectedAnnouncementNames = ref<string[]>([]);

const keyword = useRouteQuery<string>("keyword", "");
const page = useRouteQuery<number>("page", 1, { transform: Number });
const size = useRouteQuery<number>("size", 20, { transform: Number });
const selectedSort = useRouteQuery<string | undefined>("sort");
const selectedPermissions = useRouteQuery<string | undefined>("permissions");
const total = ref(0);

watch(
  () => [
    selectedPermissions.value,
    selectedSort.value,
    keyword.value,
  ],
  () => {
    page.value = 1;
  },
);

const handleClearFilters = () => {
  selectedPermissions.value = undefined;
  selectedSort.value = undefined;
};

const hasFilters = computed(() => {
  return (
    selectedPermissions.value ||
    selectedSort.value
  );
});

const {
  data: announcements,
  isLoading,
  isFetching,
  refetch,
} = useQuery({
  queryKey: [
    "announcements",
    page,
    size,
    keyword,
    selectedSort,
    selectedPermissions,
  ],
  queryFn: async () => {
    const { data } = await announcementApiClient.listAnnouncements({
      page: page.value,
      size: size.value,
      sort: selectedSort.value ? [selectedSort.value] : undefined,
      keyword: keyword.value,
      announcementSpecPermissions: selectedPermissions.value
    });
    total.value = data.total;
    return data;
  },
  refetchInterval: (query) => {
    const data = (query as any).state?.data as AnnouncementList | undefined;
    if (!data) return false;
    const hasDeletingAnnouncement = data.items?.some(
      (ann: Announcement) => (ann as any)?.metadata?.deletionTimestamp,
    );
    return hasDeletingAnnouncement ? 1000 : false;
  }
});

// Selection
const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;

  if (checked) {
    selectedAnnouncementNames.value =
      announcements.value?.items.map((announcement: Announcement) => {
        return announcement.metadata.name;
      }) || [];
  } else {
    selectedAnnouncementNames.value = [];
  }
};

const checkSelection = (announcement: Announcement) => {
  return (
    announcement.metadata.name === selectedAnnouncement.value?.metadata.name ||
    selectedAnnouncementNames.value.includes(announcement.metadata.name)
  );
};

watch(
  () => selectedAnnouncementNames.value,
  (newValue) => {
    checkAll.value = newValue.length === announcements.value?.items.length;
  }
);

const handleDeleteInBatch = async () => {
  Dialog.warning({
    title: '删除所选公告',
    description: '将同时删除所有选中的公告，该操作不可恢复。',
    confirmType: "danger",
    confirmText: '确定',
    cancelText: '取消',
    onConfirm: async () => {
      try {
        const promises = selectedAnnouncementNames.value.map((name) => {
          return announcementV1alpha1Api.deleteAnnouncement({ name });
        });
        await Promise.all(promises);
        selectedAnnouncementNames.value = [];
        Toast.success('删除成功');
      } catch (e) {
      } finally {
        refetch();
      }
    },
  });
};

const goCreate = () => {
  const base = window.location.origin + window.location.pathname.replace(/\/?$/, "");
  if (base.endsWith("/announcements")) {
    window.location.href = base + "/new";
  } else {
    window.location.href = "/console/tools/announcements/new";
  }
};
</script>
<template>
  <VPageHeader title="公告管理">
    <template #icon>
      <IconAnnouncementMegaphone class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton type="secondary" @click="goCreate">新建公告</VButton>
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <div class="block w-full bg-gray-50 px-4 py-3">
          <div
            class="relative flex flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center"
          >
            <div
              v-permission="['plugin:announcement:manage']"
              class="hidden items-center sm:flex"
            >
              <input
                v-model="checkAll"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class="flex w-full flex-1 items-center sm:w-auto">
              <SearchInput
                v-if="!selectedAnnouncementNames.length"
                v-model="keyword" />
              <VSpace v-else>
                <VButton type="danger" @click="handleDeleteInBatch">
                  删除
                </VButton>
              </VSpace>
            </div>
            <VSpace spacing="lg" class="flex-wrap">
              <FilterCleanButton
                v-if="hasFilters"
                @click="handleClearFilters"
              />
              <FilterDropdown
                v-model="selectedPermissions"
                label="权限"
                :items="[
                  {
                    label: '全部',
                    value: undefined,
                  },
                  {
                    label: '登录用户',
                    value: 'loggedInUsers',
                  },
                  {
                    label: '未登录用户',
                    value: 'nonLoggedInUsers',
                  },
                  {
                    label: '所有人',
                    value: 'everyone',
                  },
                  {
                    label: '不显示',
                    value: 'notShown',
                  },
                ]"
              />
              <FilterDropdown
                v-model="selectedSort"
                label="排序"
                :items="[
                  {
                    label: '默认',
                  },
                  {
                    label: '较近创建',
                    value: 'metadata.creationTimestamp,desc',
                  },
                  {
                    label: '较早创建',
                    value: 'metadata.creationTimestamp,asc',
                  },
                ]"
              />
              <div class="flex flex-row gap-2">
                <div
                  class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                  @click="refetch()"
                >
                  <IconRefreshLine
                    v-tooltip="'刷新'"
                    :class="{ 'animate-spin text-gray-900': isFetching }"
                    class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                  />
                </div>
              </div>
            </VSpace>
          </div>
        </div>
      </template>
      <VLoading v-if="isLoading" />
      <Transition v-else-if="!announcements?.items.length" appear name="fade">
        <VEmpty message="当前没有公告，你可以尝试刷新" title="没有公告">
          <template #actions>
            <VSpace>
              <VButton @click="refetch">
                刷新
              </VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>
      <Transition v-else appear name="fade">
        <VEntityContainer>
          <AnnouncementListItem
            v-for="announcement in announcements?.items"
            :key="announcement.metadata.name"
            :announcement="announcement"
            :is-selected="checkSelection(announcement)"
            @refresh="refetch"
          >
            <template #checkbox>
              <input
                v-model="selectedAnnouncementNames"
                :value="announcement.metadata.name"
                name="announcement-checkbox"
                type="checkbox"
              />
            </template>
          </AnnouncementListItem>
        </VEntityContainer>
      </Transition>
      <template #footer>
        <VPagination
          v-model:page="page"
          v-model:size="size"
          page-label="页"
          size-label="条 / 页"
          :total-label="`共 ${total} 项数据`"
          :total="total"
          :size-options="[20, 30, 50, 100]"
        />
      </template>
    </VCard>
  </div>

</template>
