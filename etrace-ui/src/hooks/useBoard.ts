import {reaction} from "mobx";
import {Board} from "$models/BoardModel";
import {useEffect, useState} from "react";
import StoreManager from "$store/StoreManager";

export default function useBoard() {
    const {boardStore} = StoreManager;
    const [board, setBoard] = useState<Board>(null);

    useEffect(() => {
        const disposer = reaction(
            () => boardStore.board,
            b => setBoard(b),
            {fireImmediately: true},
        );
        return () => disposer();
    }, []);

    return board;
}
