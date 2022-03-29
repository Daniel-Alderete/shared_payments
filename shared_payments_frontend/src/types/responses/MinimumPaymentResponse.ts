import AmountResponse from "./AmountResponse";

export default interface MinimumPaymentResponse {
    friendId: string;
    friendName: string;
    friendSurname: string;
    payments: Array<AmountResponse>;

}