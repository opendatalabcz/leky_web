import {Basket} from "../components/Basket"
import {TabSwitcher} from "../components/TabSwitcher"
import {DrugSelectionPanel} from "../components/DrugSelectionPanel";

export function HomePage() {
    return (
        <div>
            <DrugSelectionPanel />
            <Basket />
            <TabSwitcher />
        </div>
    )
}
