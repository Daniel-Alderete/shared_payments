<template>
  <div class="submit-form">
    <div v-if="!submitted">
      <div class="form-group">
        <label for="name">Name</label>
        <input
          type="text"
          class="form-control"
          id="name"
          required
          v-model="group.name"
          name="name"
        />
      </div>
      <div class="form-group">
        <label for="description">Description</label>
        <input
          class="form-control"
          id="description"
          required
          v-model="group.description"
          name="description"
        />
      </div>
      <button @click="saveGroup" class="btn btn-success">Submit</button>
    </div>
    <div v-else>
      <h4>You submitted a group successfully!</h4>
      <button class="btn btn-success" @click="newGroup">
        Add a new group
      </button>
    </div>
  </div>
</template>
<script lang="ts">
import GroupDataService from '@/services/GroupDataService'
import Group from '@/types/models/Group'
import ResponseData from '@/types/responses/ResponseData'
import { defineComponent } from 'vue'
export default defineComponent({
  name: 'add-group',
  data () {
    return {
      group: {
        id: null,
        name: '',
        description: ''
      } as Group,
      submitted: false
    }
  },
  methods: {
    saveGroup () {
      const data = {
        name: this.group.name,
        description: this.group.description,
        friends: []
      }
      GroupDataService.create(data)
        .then((response: ResponseData) => {
          this.group.id = response.data.data.id
          console.log(response.data.data)
          this.submitted = true
        })
        .catch((e: Error) => {
          console.log(e)
        })
    },
    newGroup () {
      this.submitted = false
      this.group = {} as Group
    }
  }
})
</script>
<style>
.submit-form {
  max-width: 300px;
  margin: auto;
}
</style>
