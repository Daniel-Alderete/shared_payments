<template>
  <div class="list row">
    <div class="col-md-8">
    </div>
    <div class="col-md-6">
      <h4>Groups List</h4>
      <ul class="list-group">
        <li
          class="list-group-item"
          :class="{ active: index == currentIndex }"
          v-for="(group, index) in groups"
          :key="index"
          @click="setActiveGroup(group, index)"
        >
          {{ group.name }}
        </li>
      </ul>
    </div>
    <div class="col-md-6">
      <div v-if="currentGroup.id">
        <h4>Group</h4>
        <div>
          <label><strong>Id:</strong></label> {{ currentGroup.id }}
        </div>
        <div>
          <label><strong>Name:</strong></label> {{ currentGroup.name }}
        </div>
        <div>
          <label><strong>Description:</strong></label>
          {{ currentGroup.description }}
        </div>
        <router-link
          :to="'/groups/' + currentGroup.id"
          >Select Group</router-link
        >
      </div>
      <div v-else>
        <br />
        <p>Please click on a Group...</p>
      </div>
    </div>
  </div>
</template>
<script lang="ts">
import { defineComponent } from "vue";
import GroupDataService from "@/services/GroupDataService";
import Group from "@/types/models/Group";
import ResponseData from "@/types/responses/ResponseData";
export default defineComponent({
  name: "groups-list",
  data() {
    return {
      groups: [] as Group[],
      currentGroup: {} as Group,
      currentIndex: -1,
      title: "",
    };
  },
  methods: {
    retrieveGroups() {
      GroupDataService.getAll()
        .then((response: ResponseData) => {
          this.groups = response.data.data.groups;
        })
        .catch((e: Error) => {
          console.log(e);
        });
    },
    refreshList() {
      this.retrieveGroups();
      this.currentGroup = {} as Group;
      this.currentIndex = -1;
    },
    setActiveGroup(group: Group, index = -1) {
      this.currentGroup = group;
      this.currentIndex = index;
    },
  },
  mounted() {
    this.retrieveGroups();
  },
});
</script>
<style>
.list {
  text-align: left;
  max-width: 750px;
  margin: auto;
}
</style>
