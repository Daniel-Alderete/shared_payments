import AmountResponse from "./AmountResponse";
import MinimumPaymentResponse from "./MinimumPaymentResponse";

export default interface GroupInfoResponse {
    debts: Array<AmountResponse>;
    minimumPayment: Array<MinimumPaymentResponse>;
}