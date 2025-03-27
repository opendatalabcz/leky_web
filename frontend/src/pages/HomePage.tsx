import {Basket} from "../components/Basket"
import {TabSwitcher} from "../components/TabSwitcher"
import {DrugSelectionPanel} from "../components/DrugSelectionPanel";

export function HomePage() {
    return (
        <div>
            <h1>Datové přehledy</h1>
            <DrugSelectionPanel />
            <Basket />
            <TabSwitcher />
        </div>
    )
}
