import {DrugSelectionPanel} from "../components/DrugSelectionPanel"
import {TabSwitcher} from "../components/TabSwitcher"

export function HomePage() {
    return (
        <div>
            <DrugSelectionPanel />
            <TabSwitcher />
        </div>
    )
}
