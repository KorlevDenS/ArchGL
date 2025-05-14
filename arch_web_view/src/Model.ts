export class Actor {
    name: string;
    type: string;

    constructor(name: string, type: string) {
        this.name = name;
        this.type = type;
    }

    toString(): string {
        return this.name +  " тип=" + this.type;
    }

    expression(): string {
        return "" +
            "   actor " + this.name + " {\n" +
            "       type: \"" + this.type + "\"\n" +
            "   }\n";
    }
}

export class Data {
    name: string;
    type: string;
    retention: number;
    unitVolume: number;

    constructor(name: string, type: string, retention: number, unitVolume: number) {
        this.name = name;
        this.type = type;
        this.retention = retention;
        this.unitVolume = unitVolume;
    }

    toString(): string {
        return this.name +  " тип=" + this.type + "; хранится=" + this.retention + "мс.; экземпляр: " +  this.unitVolume + "б.";
    }

    expression(): string {
        return "" +
            "   data " + this.name + " {\n" +
            "       type: \"" + this.type + "\"\n" +
            "       retention: " + this.retention + "\n" +
            "       unitVolume: " + this.unitVolume + "\n" +
            "   }\n";
    }
}

export class FR {
    name: string;
    frequency: number;
    actions: Action[];

    key: string;

    constructor(name: string, frequency: number, actions: Action[]) {
        this.name = name;
        this.frequency = frequency;
        this.actions = actions;
        this.key = name + Date.now().toString()
    }

    toString(): string {
        let acts = "";

        for (let i = 0; i < this.actions.length - 1; i++) {
            acts += this.actions[i].description() + ", ";
        }
        acts += this.actions[this.actions.length - 1].description();
        acts += ".";
        return this.name +  "  запросов в день=" + this.frequency + "; действия=" + acts;
    }

    expression(): string {
        let acts = "";
        for (let i = 1; i < this.actions.length - 1; i++) {
            acts += ("          " + this.actions[i].expression() + ",\n");
        }
        acts += ("          " + this.actions[this.actions.length - 1].expression() + "\n");
        return "" +
            "   fr " + this.name + " {\n" +
            "       actions: (\n" +
            acts +
            "       )\n" +
            "       frequency: " + this.frequency + "\n" +
            "   }\n";
    }
}


export abstract class Action {
    abstract expression(): string;
    abstract description(): string;
}

export abstract class Accepting extends Action {}
export abstract class Generative extends Action {}
export abstract class Intermediate extends Action {}
export abstract class Absorbing extends Action {}

export class AppMust extends Action {
    override expression(): string {
        return "";
    }

    override description(): string {
        return "Приложение должно: ";
    }
}

export class AcceptRequestFrom extends Accepting{
    sender: string;

    constructor(sender: string) {
        super();
        this.sender = sender;
    }

    override expression(): string {
        return "accept request from " + this.sender;
    }

    override description(): string {
        return "принять запрос от " + this.sender;
    }
}

export class AcceptFrom extends Accepting{
    data0: string;
    sender: string;

    constructor(data0: string, sender: string) {
        super();
        this.data0 = data0;
        this.sender = sender;
    }

    override expression(): string {
        return "accept " + this.data0 + " from " + this.sender;
    }

    override description(): string {
        return "принять " + this.data0 + " от " + this.sender;
    }
}


export class Generate extends Generative {
    data0: string;

    constructor(data0: string) {
        super();
        this.data0 = data0;
    }

    override expression(): string {
        return "generate " + this.data0;
    }

    override description(): string {
        return "создать " + this.data0;
    }
}

export class Read extends Generative {
    data0: string;

    constructor(data0: string) {
        super();
        this.data0 = data0;
    }

    override expression(): string {
        return "read " + this.data0;
    }

    override description(): string {
        return "прочитать " + this.data0;
    }
}

export class ReadRelated extends Generative {
    related: string;
    data0: string;

    constructor(related: string, data0: string) {
        super();
        this.related = related;
        this.data0 = data0;
    }

    override expression(): string {
        return "read " + this.related + " related " + this.data0;
    }

    override description(): string {
        return "прочитать связанный с " + this.related + " " + this.data0;
    }
}

export class Process extends Intermediate {
    override expression(): string {
        return "process it";
    }

    override description(): string {
        return "обработать это";
    }
}

export class ProcessObtaining extends Intermediate {
    obtaining: string;

    constructor(obtaining: string) {
        super();
        this.obtaining = obtaining;
    }

    override expression(): string {
        return "process it obtaining " + this.obtaining;
    }

    override description(): string {
        return "обработать это и получить " + this.obtaining;
    }
}

export class SendToObtaining extends Intermediate {
    receiver: string;
    obtaining: string;

    constructor(receiver: string, obtaining: string) {
        super();
        this.receiver = receiver;
        this.obtaining = obtaining;
    }

    override expression(): string {
        return "send it to " + this.receiver +  " obtaining " + this.obtaining;
    }

    override description(): string {
        return "отправить это к " + this.receiver + " и получить " + this.obtaining;
    }
}

export class SendTo extends Absorbing {
    receiver: string;

    constructor(receiver: string) {
        super();
        this.receiver = receiver;
    }

    override expression(): string {
        return "send it to " + this.receiver;
    }

    override description(): string {
        return "отправить это к " + this.receiver
    }
}

export class Save extends Absorbing {
    override expression(): string {
        return "save it";
    }

    override description(): string {
        return "сохранить это";
    }
}

export class SaveRelated extends Absorbing {
    related: string;

    constructor(related: string) {
        super();
        this.related = related;
    }

    override expression(): string {
        return "save " + this.related + " related it";
    }

    override description(): string {
        return "сохранить это и связать с " + this.related;
    }
}

export class Update extends Absorbing {
    override expression(): string {
        return "update it";
    }

    override description(): string {
        return "обновить это";
    }
}

export class UpdateRelated extends Absorbing {
    related: string;

    constructor(related: string) {
        super();
        this.related = related;
    }

    override expression(): string {
        return "update " + this.related + " related it";
    }

    override description(): string {
        return "обновить связанное с " + this.related + " это";
    }
}

export class Delete extends Absorbing {
    override expression(): string {
        return "delete it";
    }

    override description(): string {
        return "удалить это";
    }
}

export class DeleteRelated extends Absorbing {
    related: string;

    constructor(related: string) {
        super();
        this.related = related;
    }

    override expression(): string {
        return "delete " + this.related + " related it";
    }

    override description(): string {
        return "удалить связанное с " + this.related + " это";
    }
}


