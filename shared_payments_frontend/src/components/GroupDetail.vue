<template>
  <div v-if="currentGroup.id" class="edit-form">
    <h4>Group</h4>
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-4">
          <router-link :to="'/groups/' + currentGroup.id + '/addfriend'"
            >Add Friend</router-link
          >
        </div>
        <div class="col-4">
          <router-link :to="'/groups/' + currentGroup.id + '/addpayment'"
            >Add Payment</router-link
          >
        </div>
      </div>
    </div>
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
    <div>
      <label><strong>Payments:</strong></label>
      <div class="col-md-10">
        <ul class="list-group">
          <li
            class="list-group-item"
            v-for="(paymentDisplay, index) in paymentsDisplay"
            :key="index"
          >
            {{ paymentDisplay.name }}
            {{ paymentDisplay.surname }}
            {{ paymentDisplay.amount }}€
            {{ paymentDisplay.description }}
            {{ paymentDisplay.date }}
          </li>
        </ul>
      </div>
    </div>
    <div>
      <label><strong>Balance:</strong></label>
      <div class="col-md-8">
        <ul class="list-group">
          <li
            class="list-group-item"
            v-for="(debt, index) in groupInfo.debts"
            :key="index"
          >
            {{ debt.friendName }}
            {{ debt.friendSurname }}
            {{ debt.amount }}€
          </li>
        </ul>
      </div>
    </div>
    <div>
      <label><strong>Minimum Payments:</strong></label>
      <div class="col-md-10">
        <ul class="list-group">
          <li
            class="list-group-item"
            v-for="(minimumPayment, index) in groupInfo.minimumPayment"
            :key="index"
          >
            {{ minimumPayment.friendName }}
            {{ minimumPayment.friendSurname }}
            <div class="col-md-8">
              <ul class="list-group">
                <li
                  class="list-group-item"
                  v-for="(payment, index) in minimumPayment.payments"
                  :key="index"
                >
                  {{ payment.friendName }}
                  {{ payment.friendSurname }}
                  {{ payment.amount }}€
                </li>
              </ul>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
  <div v-else>
    <br />
    <p>Please click on a Group...</p>
  </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import ResponseData from '@/types/responses/ResponseData'
import GroupInfoResponse from '@/types/responses/GroupInfoResponse'
import PaymentDisplay from '@/types/models/PaymentDisplay'
import Group from '@/types/models/Group'
import GroupDataService from '@/services/GroupDataService'
export default defineComponent({
  name: 'group-list',
  data () {
    return {
      currentGroup: {} as Group,
      groupInfo: {} as GroupInfoResponse,
      paymentsDisplay: [] as PaymentDisplay[],
      message: ''
    }
  },
  methods: {
    getGroup (id: any) {
      GroupDataService.get(id)
        .then((response: ResponseData) => {
          this.currentGroup = response.data.data
          console.log(response.data)

          this.currentGroup.friends.forEach((friend) => {
            friend.payments.forEach((payment) => {
              const moment = require('moment')
              const dateFormatted: string = moment
                .utc(payment.date)
                .local()
                .fromNow()
              console.log(dateFormatted)

              const paymentDisplay: PaymentDisplay = {
                name: friend.name,
                surname: friend.surname,
                amount: payment.amount,
                description: payment.description,
                date: dateFormatted
              }

              this.paymentsDisplay.push(paymentDisplay)
            }, this)
          }, this)
        })
        .catch((e: Error) => {
          console.log(e)
        })
      GroupDataService.getInfo(id)
        .then((response: ResponseData) => {
          this.groupInfo = response.data.data
          console.log(this.groupInfo)
        })
        .catch((e: Error) => {
          console.log(e)
        })
    }
  },
  mounted () {
    this.message = ''
    this.getGroup(this.$route.params.id)
  }
})
</script>
<style>
.edit-form {
  max-width: 300px;
  margin: auto;
}
</style>
