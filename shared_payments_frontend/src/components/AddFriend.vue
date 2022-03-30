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
          v-model="friend.name"
          name="name"
        />
      </div>
      <div class="form-group">
        <label for="surname">Surname</label>
        <input
          class="form-control"
          id="surname"
          required
          v-model="friend.surname"
          name="surname"
        />
      </div>
      <button @click="saveFriend" class="btn btn-success">Submit</button>
    </div>
    <div v-else>
      <h4>You submitted a friend successfully!</h4>
      <button class="btn btn-success" @click="newFriend">
        Add a new friend
      </button>
    </div>
  </div>
</template>
<script lang="ts">
import FriendDataService from '@/services/FriendDataService'
import Friend from '@/types/models/Friend'
import ResponseData from '@/types/responses/ResponseData'
import { defineComponent } from 'vue'
export default defineComponent({
  name: 'add-friend',
  data () {
    return {
      friend: {
        id: null,
        name: '',
        surname: ''
      } as Friend,
      submitted: false,
      currentGroupId: ''
    }
  },
  methods: {
    saveFriend (id: any) {
      const data = {
        name: this.friend.name,
        surname: this.friend.surname,
        payments: []
      }
      FriendDataService.create(this.currentGroupId, data)
        .then((response: ResponseData) => {
          this.friend.id = response.data.data.id
          console.log(response.data.data)
          this.submitted = true
        })
        .catch((e: Error) => {
          console.log(e)
        })
    },
    newFriend () {
      this.submitted = false
      this.friend = {} as Friend
    }
  },
  mounted () {
    this.currentGroupId = this.$route.params.id as string
  }
})
</script>
<style>
.submit-form {
  max-width: 300px;
  margin: auto;
}
</style>
