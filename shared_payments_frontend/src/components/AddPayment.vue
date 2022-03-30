<template>
  <div class="submit-form">
    <div v-if="!submitted">
      <div class="form-group">
        <label for="amount">Amount</label>
        <input
          type="number"
          step="0.01"
          class="form-control"
          id="amount"
          required
          v-model="payment.amount"
          name="amount"
        />
      </div>
      <div class="form-group">
        <label for="description">Description</label>
        <input
          class="form-control"
          id="description"
          required
          v-model="payment.description"
          name="description"
        />
      </div>
      <div class="form-group">
        <label for="date">Date</label>
        <input
          type="datetime-local"
          class="form-control"
          id="date"
          required
          name="date"
          v-model="payment.date"
        />
      </div>
      <div class="col-md-6">
        <h4>Friend List</h4>
        <ul class="list-group">
          <li
            class="list-group-item"
            :class="{ active: index == currentIndex }"
            v-for="(friend, index) in friends"
            :key="index"
            @click="setActiveFriend(friend, index)"
          >
            {{ friend.name }}
            {{ friend.surname }}
          </li>
        </ul>
      </div>
      <button @click="savePayment" class="btn btn-success">Submit</button>
    </div>
    <div v-else>
      <h4>You submitted a payment successfully!</h4>
      <button class="btn btn-success" @click="newPayment">
        Add a new payment
      </button>
    </div>
  </div>
</template>
<script lang="ts">
import FriendDataService from '@/services/FriendDataService'
import PaymentDataService from '@/services/PaymentDataService'
import Friend from '@/types/models/Friend'
import Payment from '@/types/models/Payment'
import ResponseData from '@/types/responses/ResponseData'
import { defineComponent } from 'vue'
export default defineComponent({
  name: 'add-payment',
  data () {
    return {
      payment: {
        id: null,
        amount: 0,
        description: '',
        date: 0
      } as Payment,
      submitted: false,
      currentGroupId: '',
      friends: [] as Friend[],
      currentFriend: {} as Friend,
      currentIndex: -1
    }
  },
  methods: {
    savePayment (id: any) {
      const data = {
        amount: this.payment.amount,
        description: this.payment.description,
        date:
          new Date(new Date(this.payment.date).toUTCString()).getTime() / 1000
      }
      PaymentDataService.create(
        this.currentGroupId,
        this.currentFriend.id,
        data
      )
        .then((response: ResponseData) => {
          this.payment.id = response.data.data.id
          console.log(response.data.data)
          this.submitted = true
        })
        .catch((e: Error) => {
          console.log(e)
        })
    },
    newPayment () {
      this.submitted = false
      this.payment = {} as Payment
    },
    retrieveFriends (id: any) {
      FriendDataService.getAll(id)
        .then((response: ResponseData) => {
          this.friends = response.data.data.friends
        })
        .catch((e: Error) => {
          console.log(e)
        })
    },
    setActiveFriend (friend: Friend, index = -1) {
      this.currentFriend = friend
      this.currentIndex = index
    }
  },
  mounted () {
    this.currentGroupId = this.$route.params.id as string
    this.retrieveFriends(this.currentGroupId)
  }
})
</script>
<style>
.submit-form {
  max-width: 300px;
  margin: auto;
}
</style>
