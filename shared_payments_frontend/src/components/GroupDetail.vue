<template>
  <div v-if="currentGroup.id" class="edit-form">
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
    <div>
      <label><strong>Payments:</strong></label>
      <div class="col-md-6">
        <ul class="list-group">
          <li
            class="list-group-item"
            v-for="(friend, index) in currentGroup.friends"
            :key="index"
          >
            {{ friend.name }}
            {{ friend.surname }}
          </li>
        </ul>
      </div>
    </div>
    <div>
      <label><strong>Balance:</strong></label>
      <div class="col-md-6">
        <ul class="list-group">
          <li
            class="list-group-item"
            v-for="(debt, index) in groupInfo.debts"
            :key="index"
          >
            {{ debt.name }}
            {{ debt.surname }}
            {{ debt.amount }}
          </li>
        </ul>
      </div>
    </div>
    <div>
      <label><strong>Minimum Payments:</strong></label>
      <div class="col-md-6">
        <ul class="list-group">
          <li
            class="list-group-item"
            v-for="(minimumPayment, index) in groupInfo.minimumPayment"
            :key="index"
          >
            {{ minimumPayment.name }}
            {{ minimumPayment.surname }}
            <div class="col-md-6">
              <ul class="list-group">
                <li
                  class="list-group-item"
                  v-for="(payments, index) in minimumPayment.payments"
                  :key="index"
                >
                  {{ minimumPayment.name }}
                  {{ minimumPayment.surname }}
                  {{ minimumPayment.amount }}
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
import { defineComponent } from "vue";
import ResponseData from "@/types/responses/ResponseData";
import GroupInfoResponse from "@/types/responses/GroupInfoResponse";
import PaymentDisplay from "@/types/models/PaymentDisplay";
import Group from "@/types/models/Group";
import GroupDataService from "@/services/GroupDataService";
export default defineComponent({
  name: "group",
  data() {
    return {
      currentGroup: {} as Group,
      groupInfo: {} as GroupInfoResponse,
      paymentsDisplay: [] as PaymentDisplay[],
      message: "",
    };
  },
  methods: {
    getGroup(id: any) {
      GroupDataService.get(id)
        .then((response: ResponseData) => {
          this.currentGroup = response.data.data;
          console.log(response.data);

          this.currentGroup.friends.forEach((friend) => {
            friend.payments.forEach((payment) => {
              const moment = require("moment");
              let dateFormatted: string = moment
                .utc(payment.date)
                .local()
                .fromNow();
              console.log(dateFormatted);

              let paymentDisplay: PaymentDisplay = {
                name: friend.name,
                surname: friend.surname,
                amount: payment.amount,
                description: payment.description,
                date: dateFormatted,
              };

              this.paymentsDisplay.push(paymentDisplay);//recorrer este array para ir poniendo los pagos
            }, this);
          }, this);
        })
        .catch((e: Error) => {
          console.log(e);
        });
      GroupDataService.getInfo(id)
        .then((response: ResponseData) => {
          this.groupInfo = response.data.data;
          console.log(response.data);
        })
        .catch((e: Error) => {
          console.log(e);
        });
    },
  },
  mounted() {
    this.message = "";
    this.getGroup(this.$route.params.id);
  },
});
</script>
<style>
.edit-form {
  max-width: 300px;
  margin: auto;
}
</style>
