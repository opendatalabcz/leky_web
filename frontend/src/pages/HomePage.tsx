import {DrugSelectionPanel} from "../components/DrugSelectionPanel"
import {TabSwitcher} from "../components/TabSwitcher"
import {VisualizationSettings} from "../components/VisualizationSettings"

export function HomePage() {
    return (
        <div>
            <DrugSelectionPanel />
            <VisualizationSettings />
            <TabSwitcher />
        </div>
    )
}
