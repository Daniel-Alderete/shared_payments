import Friend from "./Friend";

export default interface Group {
    id: null;
    name: string;
    description: string;
    friends: Array<Friend>;
  }