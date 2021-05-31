import {useContext} from "react";
import {UserContext} from "../Context";

export default function useUser() {
    return useContext(UserContext);
}
