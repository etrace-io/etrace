
export interface Serializable<T> {
    deserialize(input: Object): T;
}

export class MetricV2 implements Serializable<MetricV2> {

    public endTime: number;
    public interval: number;
    public metricName: string;
    public metricType: MetricType;
    public groupedPoints: Array<GroupedPoint>;
    public points: Points;
    public startTime: number;
    public pointCount: number;
    private _hasData = true;

    public static parseMetricType(type: string): MetricType {
        switch (type) {
            case "counter":
                return MetricType.Counter;
            case "timer":
                return MetricType.Timer;
            case "apdex":
                return MetricType.Apdex;
            case "gauge":
                return MetricType.Gauge;
            case "payload":
                return MetricType.Payload;
            case "ratio":
                return MetricType.Ratio;
            case "histogram":
                return MetricType.Histogram;
            default:
                return null;
        }
    }

    deserialize(o: any): MetricV2 {
        this.endTime = o.endTime;
        this.interval = o.interval;
        this.metricName = o.metricName;
        this.pointCount = o.pointCount;
        this.metricType = MetricV2.parseMetricType(o.metricType);

        if (o.groupedPoints) {
            this.groupedPoints = [];
            for (let groupedPoint of o.groupedPoints) {
                // delete
                if (new GroupedPoint().checkPointsValidate(groupedPoint)) {
                    this.groupedPoints.push(new GroupedPoint().deserializeByType(groupedPoint, this.metricType));
                }
            }
        } else if (o.points) {
            this.points = new Points().deserializeByType(o.points, this.metricType);
        } else {
            this._hasData = false;
        }
        this.startTime = o.startTime;
        return this;
    }

    public hasData(): boolean {
        return this._hasData;
    }

    public isPoint(): boolean {
        return this.hasData() && this.points != null;
    }

    public isGrouped(): boolean {
        return this.hasData() && this.groupedPoints != null;
    }
}

export enum MetricType {
    Counter, Timer, Apdex, Gauge, Payload, Ratio, Histogram
}

export class Points implements Serializable<Points> {
    public summary: Point;
    public values: Array<Point>;

    deserializeByType(input: any, metricType: MetricType): Points {
        this.summary = Point.deserializeByType(input.summary, metricType);

        if (input.values) {
            this.values = [];

            for (let value of input.values) {
                this.values.push(Point.deserializeByType(value, metricType));
            }
        }

        return this;
    }

    deserialize(input: any): Points {
        return null;
    }
}

export abstract class Point implements Serializable<Point> {
    public static deserializeByType(input: any, metricType: MetricType): Point {
        switch (metricType) {
            case MetricType.Apdex :
                return new ApdexPoint().deserialize(input);
            case MetricType.Counter:
                return new CounterPoint().deserialize(input);
            case MetricType.Timer:
                return new TimerPoint().deserialize(input);
            case MetricType.Gauge:
                return new GaugePoint().deserialize(input);
            case MetricType.Payload:
                return new PayloadPoint().deserialize(input);
            case MetricType.Ratio:
                return new RatioPoint().deserialize(input);
            case MetricType.Histogram:
                return new HistogramPoint().deserialize(input);
            default:
                return null;
        }
    }

    public abstract getValue(statType: number): number;

    public abstract getValueByKey(type: number): number;

    public abstract merge(other: Point): Point;

    deserialize(input: any): Point {
        return null;
    }

    public abstract zero(): Point;
}

export class CounterPoint implements Point {
    public value: number;

    getValue(statType: number): number {
        return this.value;
    }

    getValueByKey(type: number): number {
        return this.value;
    }

    merge(other: CounterPoint): CounterPoint {
        this.value += other.value;
        return this;
    }

    deserialize(input: any): Point {
        this.value = input;
        return this;
    }

    deserializeByType(input: any, metricType: MetricType): Point {
        return undefined;
    }

    public zero(): CounterPoint {
        let zero: CounterPoint = new CounterPoint;
        zero.value = 0;
        return zero;
    }
}

export class TimerPoint implements Point {
    public avg: number;
    public count: number;
    public max: number;
    public min: number;
    public sum: number;

    getValue(statType: number): number {
        if (statType == 1) {
            return this.avg;
        } else if (statType == 5) {
            return this.max;
        }
        return this.count;
    }

    getValueByKey(type: number): number {
        if (type == 1) {
            return this.avg;
        } else if (type == 2) {
            return this.sum;
        } else if (type == 3) {
            return this.count;
        } else if (type == 4) {
            return this.max;
        } else if (type == 5) {
            return this.min;
        }
        return this.count;
    }

    merge(other: TimerPoint): TimerPoint {
        this.count += other.count;
        this.sum += other.sum;
        this.max = this.max > other.max ? this.max : other.max;
        this.min = this.min < other.min ? this.min : other.min;
        this.avg = this.sum / this.count;
        return this;
    }

    deserialize(input: any): Point {
        if (input) {
            this.avg = input.avg;
            this.count = input.count;
            this.max = input.max;
            this.min = input.min;
            this.sum = input.sum;
        }
        return this;
    }

    deserializeByType(input: any, metricType: MetricType): Point {
        return undefined;
    }

    public zero(): TimerPoint {
        let zero: TimerPoint = new TimerPoint;
        zero.avg = 0;
        zero.count = 0;
        zero.max = 0;
        zero.min = 0;
        zero.sum = 0;
        return zero;
    }
}

export class HistogramPoint implements Point {
    public avg: number;
    public count: number;
    public max: number;
    public min: number;
    public sum: number;
    public upper_95: number;  // tslint:disable-line

    getValue(statType: number): number {
        if (statType == 1) {
            return this.avg;
        } else if (statType == 5) {
            return this.max;
        }
        return this.upper_95;
    }

    getValueByKey(type: number): number {
        if (type == 1) {
            return this.avg;
        } else if (type == 2) {
            return this.sum;
        } else if (type == 3) {
            return this.count;
        } else if (type == 4) {
            return this.max;
        } else if (type == 5) {
            return this.min;
        } else if (type == 6) {
            return this.upper_95;
        }
        return this.upper_95;
    }

    merge(other: HistogramPoint): HistogramPoint {
        this.count += other.count;
        this.sum += other.sum;
        this.upper_95 += other.upper_95;
        this.max = this.max > other.max ? this.max : other.max;
        this.min = this.min < other.min ? this.min : other.min;
        this.avg = this.sum / this.count;
        return this;
    }

    deserialize(input: any): Point {
        if (input) {
            this.avg = input.avg;
            this.count = input.count;
            this.max = input.max;
            this.min = input.min;
            this.sum = input.sum;
            this.upper_95 = input.upper_95;
        }
        return this;
    }

    deserializeByType(input: any, metricType: MetricType): Point {
        return undefined;
    }

    public zero(): HistogramPoint {
        let zero: HistogramPoint = new HistogramPoint;
        zero.avg = 0;
        zero.count = 0;
        zero.max = 0;
        zero.min = 0;
        zero.sum = 0;
        zero.upper_95 = 0;
        return zero;
    }
}

export class ApdexPoint implements Point {

    public apdex: number;
    public frustrated: number;
    public satisfied: number;
    public tolerating: number;
    public total: number;

    getValue(statType: number): number {
        return undefined;
    }

    getValueByKey(type: number): number {
        return undefined;
    }

    merge(other: ApdexPoint): ApdexPoint {
        this.total += other.total;
        this.satisfied += other.satisfied;
        this.frustrated += other.frustrated;
        this.tolerating += other.tolerating;
        this.apdex = (this.satisfied + this.tolerating / 2) / this.total;
        return this;
    }

    deserialize(input: any): Point {
        if (input) {
            this.apdex = input.apdex;
            this.frustrated = input.frustrated;
            this.satisfied = input.satisfied;
            this.tolerating = input.tolerating;
            this.total = input.total;
        }
        return this;
    }

    deserializeByType(input: any, metricType: MetricType): Point {
        return undefined;
    }

    public zero(): ApdexPoint {
        let zero: ApdexPoint = new ApdexPoint;
        zero.apdex = 0;
        zero.frustrated = 0;
        zero.satisfied = 0;
        zero.tolerating = 0;
        zero.total = 0;
        return zero;
    }
}

export class GaugePoint implements Point {
    public value: number;
    public timestamp: number;

    getValue(statType: number): number {
        return undefined;
    }

    getValueByKey(type: number): number {
        return this.value;
    }

    merge(other: GaugePoint): GaugePoint {
        this.value += other.value;
        return this;
    }

    deserialize(input: any): Point {
        if (input) {
            this.value = input.value;
            this.timestamp = input.time;
        }
        return this;
    }

    zero(): GaugePoint {
        let zero: GaugePoint = new GaugePoint;
        zero.value = 0;
        return zero;
    }

}

export class PayloadPoint implements Point {
    public avg: number;
    public count: number;
    public max: number;
    public min: number;
    public sum: number;

    getValue(statType: number): number {
        if (statType == 1) {
            return this.avg;
        } else if (statType == 2) {
            return this.count;
        } else if (statType == 3) {
            return this.sum;
        }
        return this.count;
    }

    getValueByKey(type: number): number {
        if (type == 1) {
            return this.avg;
        } else if (type == 2) {
            return this.sum;
        } else if (type == 3) {
            return this.count;
        } else if (type == 4) {
            return this.max;
        } else if (type == 5) {
            return this.min;
        }
        return this.count;
    }

    merge(other: PayloadPoint): PayloadPoint {
        this.count += other.count;
        this.sum += other.sum;
        this.max = this.max > other.max ? this.max : other.max;
        this.min = this.min < other.min ? this.min : other.min;
        this.avg = this.sum / this.count;
        return this;
    }

    deserialize(input: any): Point {
        if (input) {
            this.avg = input.avg;
            this.count = input.count;
            this.max = input.max;
            this.min = input.min;
            this.sum = input.sum;
        }
        return this;
    }

    deserializeByType(input: any, metricType: MetricType): Point {
        return undefined;
    }

    public zero(): PayloadPoint {
        let zero: PayloadPoint = new PayloadPoint;
        zero.avg = 0;
        zero.count = 0;
        zero.max = 0;
        zero.min = 0;
        zero.sum = 0;
        return zero;
    }
}

export class RatioPoint implements Point {
    public numerator: number;
    public denominator: number;
    public value: number;

    getValue(statType: number): number {
        return undefined;
    }

    getValueByKey(type: number): number {
        return undefined;
    }

    merge(other: RatioPoint): RatioPoint {
        this.numerator += other.numerator;
        this.denominator += other.denominator;
        return this;
    }

    deserialize(input: any): Point {
        if (input) {
            this.numerator = input.numerator;
            this.denominator = input.denominator;
            this.value = input.value;
        }
        return this;
    }

    zero(): RatioPoint {
        let zero: RatioPoint = new RatioPoint;
        this.numerator = 0;
        this.denominator = 0;
        return zero;
    }

}

export class GroupedPoint implements Serializable<GroupedPoint> {
    public groupBy: Map<string, string>;
    public points: Points;

    deserialize(input: Object): GroupedPoint {
        return undefined;
    }

    checkPointsValidate(input: any): boolean {
        if (!input.points) {
            return false;
        } else if (!input.points.summary) {
            return false;
        }
        return true;
    }

    deserializeByType(input: any, metricType: MetricType): GroupedPoint {
        this.groupBy = input.groupBy;
        this.points = new Points().deserializeByType(input.points, metricType);
        return this;
    }
}
