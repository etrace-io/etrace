import {UserActionStore} from "./UserActionStore";
import {ChartStore} from "./ChartStore";
import {BoardStore} from "./BoardStore";
import {EditChartStore} from "./EditChartStore";
import {MonitorEntityStore} from "./MonitorEntityStore";
import {StatListStore} from "./StatListStore";
import {ChartEventStore} from "./ChartEventStore";
import {EventStore} from "./EventStore";
import {UserStore} from "./UserStore";
import {ProductLineStore} from "./ProductLineStore";
import {CallstackStore} from "./CallstackStore";
import {OrderByStore} from "./OrderByStore";
import {DataAppStore} from "./DataAppStore";
import {StateLinkStore} from "./StateLinkStore";
import {URLParamStore} from "./URLParamStore";
import {BoardConfigStore} from "./BoardConfigStore";
import {PageSwitchStore} from "./PageSwitchStore";
import {ConfigStore} from "./ConfigStore";
import {LoadingStore} from "./alert/LoadingStore";
import {GraphStore} from "./GraphStore";
import {NodeStore} from "./NodeStore";

class StoreManager {
    public userActionStore: UserActionStore = new UserActionStore();
    public orderByStore: OrderByStore = new OrderByStore();
    public userStore: UserStore = new UserStore();
    public configStore: ConfigStore = new ConfigStore();

    public pageSwitchStore: PageSwitchStore = new PageSwitchStore();
    public urlParamStore: URLParamStore = new URLParamStore();
    public chartStore: ChartStore = new ChartStore(this.orderByStore, this.urlParamStore);
    public statListStore: StatListStore = new StatListStore(this.urlParamStore);
    public boardStore: BoardStore = new BoardStore(this.urlParamStore, this.pageSwitchStore);
    public editChartStore: EditChartStore = new EditChartStore(this.pageSwitchStore);
    public monitorEntityStore: MonitorEntityStore = new MonitorEntityStore();
    public productLineStore: ProductLineStore = new ProductLineStore(this.userStore);

    public chartEventStore: ChartEventStore = new ChartEventStore();
    public eventStore: EventStore = new EventStore(this.userStore);
    public loadingStore: LoadingStore = new LoadingStore();
    public callstackStore: CallstackStore = new CallstackStore(this.chartEventStore);
    public dataAppStore: DataAppStore = new DataAppStore();
    public stateLinkStore: StateLinkStore = new StateLinkStore();
    public boardConfigStore: BoardConfigStore = new BoardConfigStore(this.userStore);

    public nodeStore: NodeStore = new NodeStore(this.urlParamStore);
    public graphStore: GraphStore = new GraphStore(this.urlParamStore, this.nodeStore);
}

export default new StoreManager();