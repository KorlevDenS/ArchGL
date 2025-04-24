import {
    Box,
    Button,
    Checkbox, Chip,
    Divider, FormControl,
    FormControlLabel,
    IconButton, InputLabel, ListItem, MenuItem, Paper, Select,
    Stack,
    styled,
    TextField,
    Toolbar,
    Typography
} from "@mui/material";
import MuiDrawer from '@mui/material/Drawer';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import PlantUMLDiagram from "./PlantUMLDiagram";
import {useState} from "react";

class ArchGLProgram {
    program: string

    constructor(program: string) {
        this.program = program;
    }
}

class TextUmlRequest {
    uml: string

    constructor(uml: string) {
        this.uml = uml;
    }
}

class InfoResponse {
    code: number | undefined;
    message: string;

    constructor(code: number | undefined, message: string) {
        this.code = code;
        this.message = message;
    }
}

class Actor {
    name: string;
    type: string;

    constructor(name: string, type: string) {
        this.name = name;
        this.type = type;
    }

    toString(): string {
        return this.name +  " тип=" + this.type
    }
}

class Data {
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
        return this.name +  " тип=" + this.type + " время хранения=" + this.retention + " объём экземпляра: " +  this.unitVolume
    }

}

const drawerWidth = 500;

const Main = styled('main', {shouldForwardProp: (prop) => prop !== 'open'})<{
    open?: boolean;
}>(({theme}) => ({
    flexGrow: 1,
    padding: theme.spacing(3),
    transition: theme.transitions.create('margin', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
    }),
    marginLeft: `-${drawerWidth}px`,
    variants: [
        {
            props: ({open}) => open,
            style: {
                transition: theme.transitions.create('margin', {
                    easing: theme.transitions.easing.easeOut,
                    duration: theme.transitions.duration.enteringScreen,
                }),
                marginLeft: 0,
            },
        },
    ],
}));

const DrawerHeader = styled('div')(({theme}) => ({
    display: 'flex',
    alignItems: 'center',
    padding: theme.spacing(0, 1),
    // necessary for content to be below app bar
    ...theme.mixins.toolbar,
    justifyContent: 'flex-end',
}));


interface ErrorLayoutProps {
    requestMessage: InfoResponse;
}

function ErrorLayout({requestMessage}: ErrorLayoutProps) {
    return (
        <Stack spacing={2}>
            {requestMessage.code !== undefined && (
                <Typography variant="h4" color={"info"}>
                    {"HTTP CODE: " + requestMessage.code}
                </Typography>
            )}
            <Typography variant="h6" color={"info"}>
                {requestMessage.message}
            </Typography>
        </Stack>
    );

}

interface ArchConstructorProps {
    open: boolean;

    handleDrawerChange(isOpen: boolean): void;
}


export default function ArchConstructor({open, handleDrawerChange}: ArchConstructorProps) {

    const program = "app NewsFeedApp {\n" +
        "            usersNumber: 10000000\n" +
        "            scaleVertically: \"no\"\n" +
        "            scaleHorizontally: \"yes\"\n" +
        "            latency: 99.999\n" +
        "            dayUsersNumber: 500000\n" +
        "            availability: 98.5\n" +
        "            \n" +
        "            actor User {\n" +
        "                type: \"web-client\"\n" +
        "            }\n" +
        "            \n" +
        "            data Post {\n" +
        "                type: \"text\"\n" +
        "                retention: 157680000000\n" +
        "                unitVolume: 16000\n" +
        "            }\n" +
        "            \n" +
        "            data NewsFeed {\n" +
        "                type: \"text\"\n" +
        "                retention: 60000\n" +
        "                unitVolume: 480000\n" +
        "            }\n" +
        "            \n" +
        "            data Notification {\n" +
        "                type: \"text\"\n" +
        "                retention: 0\n" +
        "                unitVolume: 320\n" +
        "            }\n" +
        "            \n" +
        "            fr PublishNewPost {\n" +
        "                actions: (\n" +
        "                    accept Post from User,\n" +
        "                    save it,\n" +
        "                    process it obtaining NewsFeed,\n" +
        "                    update User related it\n" +
        "                )\n" +
        "                frequency: 1100\n" +
        "            }\n" +
        "           \n" +
        "            fr PublishNewPost {\n" +
        "                actions: (\n" +
        "                    accept Post from User,\n" +
        "                    process it obtaining Notification,\n" +
        "                    send it to User \n" +
        "                )\n" +
        "                frequency: 1100\n" +
        "            }\n" +
        "            \n" +
        "        }";

    const [puml, setPuml] = useState<string>('A -> B: Hello');
    const [info, setInfo] = useState<InfoResponse | null>(new InfoResponse(
        undefined, "Введите требования к вашему приложению")
    );

    const [usersNumber, setUsersNumber] = useState<number>(0);
    const [scaleVertically, setScaleVertically] = useState<boolean>(false);
    const [scaleHorizontally, setScaleHorizontally] = useState<boolean>(false);
    const [latency, setLatency] = useState<number>(0);
    const [dayUsersNumber, setDayUsersNumber] = useState<number>(0);
    const [availability, setAvailability] = useState<number>(0);

    const [chipActors, setChipActors] = useState<Actor[]>([]);
    const handleDeleteActor = (chipToDelete: Actor) => () => {
        setChipActors((chips) => chips.filter((chip) => chip.name !== chipToDelete.name));
    };
    const handleAddActor = () => {
        if (userName === "") {
            setInfo(new InfoResponse(undefined, "Название актора не должно быть пустым"));
            return;
        }
        if (!/^[A-Z]/.test(userName)) {
            setInfo(new InfoResponse(undefined, "Название актора должно начинаться с заглавной латинской буквы"));
            return;
        }
        if (!/^[A-Za-z0-9]+$/.test(userName)) {
            setInfo(new InfoResponse(undefined, "Название актора может содержать только латинские буквы или цифры"));
            return;
        }
        if (chipActors.some(obj => obj.name === userName) || chipData.some(obj => obj.name === userName)) {
            setInfo(new InfoResponse(undefined, "Название пользователя должно быть уникальным"));
            return;
        }
        if (userType === "") {
            setInfo(new InfoResponse(undefined, "Тип актора не должен быть пустым"));
            return;
        }
        const newArr: Actor[] = chipActors.slice();
        newArr.push(new Actor(userName, userType));
        setChipActors(newArr);
        setInfo(new InfoResponse(undefined, "Введите требования к вашему приложению"));
    }

    const [chipData, setChipData] = useState<Data[]>([]);

    const [userName, setUserName] = useState<string>("");
    const [userType, setUserType] = useState<string>("");

    const [dataName, setDataName] = useState<string>("");
    const [dataType, setDataType] = useState<string>("");
    const [dataRetention, setDataRetention] = useState<string>("");
    const [dataUnitVolume, setDataUnitVolume] = useState<string>("");



    const getGeneratedUml = async () => {
        try {
            await fetch("http://localhost:8080/arch/gateway/generate", {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(new ArchGLProgram(program)),
            }).then(response => {
                if (response.ok) {
                    const responseData = response.json();
                    responseData.then(value => {
                        setPuml(value.uml);
                        setInfo(null);
                    })
                } else {
                    const responseData = response.json();
                    responseData.then(value => {
                        setPuml("");
                        setInfo(value as InfoResponse);
                    })
                }
            });
        } catch (error) {
            setPuml("");
            setInfo(new InfoResponse(504, "Error sending data: " + error));
        }
    }

    const handleSubmit = () => {
        setInfo(new InfoResponse(undefined, "Выполняется генерация"))
        getGeneratedUml().then();
    }

    return (
        <Box sx={{display: 'flex'}}>
            <MuiDrawer
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: drawerWidth,
                        boxSizing: 'border-box',
                    },
                }}
                variant="persistent"
                anchor="left"
                open={open}
            >
                <Toolbar sx={{marginTop: '1rem'}}/>
                <DrawerHeader>
                    <Typography
                        variant="h5"
                        noWrap
                        component="a"
                        sx={{
                            mr: 2,
                            display: {xs: 'none', md: 'flex'},
                            fontFamily: 'monospace',
                            fontWeight: 'normal',
                            letterSpacing: '.2rem',
                            color: 'inherit',
                            textDecoration: 'none',
                        }}
                    >
                        Архитектурные требования
                    </Typography>
                    <IconButton onClick={() => handleDrawerChange(false)}>
                        <ChevronLeftIcon/>
                    </IconButton>
                </DrawerHeader>
                <Divider/>

                <Stack spacing={2} mr={3} ml={3}>

                    <FormControlLabel
                        labelPlacement="start"
                        label="Вертикально масшибировать"
                        control={
                            <Checkbox checked={scaleVertically} onChange={() => {setScaleVertically(!scaleVertically)}} color="primary"/>
                        }
                    />
                    <FormControlLabel
                        labelPlacement="start"
                        label="Горизонтально масшибировать"
                        control={
                            <Checkbox checked={scaleHorizontally} onChange={() => {setScaleHorizontally(!scaleHorizontally)}} color="primary"/>
                        }
                    />
                    <TextField variant="standard" color={"primary"}
                               label="Общее количество пользователей" type={"number"}
                               onChange={(e) => {setUsersNumber(Number(e.target.value))}}/>
                    <TextField variant="standard" color={"primary"}
                               label="Латентность" type={"number"}
                               helperText="99.0% <= x < 100%"
                               onChange={(e) => {setLatency(Number(e.target.value))}}/>
                    <TextField variant="standard" color={"primary"}
                               label="Дневное количество пользователей" type={"number"}
                               onChange={(e) => {setDayUsersNumber(Number(e.target.value))}}/>
                    <TextField variant="standard" color={"primary"}
                               label="Доступность" type={"number"}
                               helperText="обычно 98.0% <= x < 100%"
                               onChange={(e) => {setAvailability(Number(e.target.value))}}/>

                    <Divider/>

                    <Typography
                        variant="h6"
                        noWrap
                        component="a"
                        sx={{
                            mr: 2,
                            display: {xs: 'none', md: 'flex'},
                            fontFamily: 'monospace',
                            fontWeight: 'normal',
                            letterSpacing: '.2rem',
                            color: 'inherit',
                            textDecoration: 'none',
                        }}
                    >
                        Добавьте акторов
                    </Typography>

                    <Box display="flex"
                         flexDirection="row"
                         alignItems="center"
                    >
                        <TextField sx={{mr: 9 }} variant="standard" color={"primary"}
                                   label="Название актора"
                                   onChange={(e) => {setUserName(e.target.value)}}/>
                        <FormControl variant="standard" sx={{minWidth: 200 }}>
                            <InputLabel id="demo-simple-select-label">Тип актора</InputLabel>
                            <Select
                                labelId="demo-simple-select-label"
                                id="demo-simple-select"
                                value={userType}
                                label="Тип актора"
                                onChange={e => {setUserType(e.target.value)}}
                            >
                                <MenuItem value={"web-client"}>Веб-клиент</MenuItem>
                                <MenuItem value={"service"}>Сервис</MenuItem>
                                <MenuItem value={"scheduler"}>Планировщик задач</MenuItem>
                            </Select>
                        </FormControl>
                    </Box>
                    <Button variant="outlined" onClick={() => {handleAddActor()}}>Добавить актора</Button>

                    <Paper
                        sx={{
                            display: 'flex',
                            justifyContent: 'center',
                            flexWrap: 'wrap',
                            listStyle: 'none',
                            p: 0.5,
                            m: 0,
                        }}
                        component="ul"
                    >
                        {chipActors.map((actor) => {
                            return (
                                <ListItem sx={{width: "inherit", p: 0.5}} key={actor.name}>
                                    <Chip
                                        label={actor.toString()}
                                        onDelete={handleDeleteActor(actor)}
                                    />
                                </ListItem>
                            );
                        })}
                    </Paper>


                    <Button variant="contained" onClick={() => {handleSubmit()}}>Сгенерировать</Button>
                </Stack>
            </MuiDrawer>
            <Main open={open} >
                <Toolbar sx={{marginTop: '1rem'}}/>
                <Box alignItems="center" justifyContent="center" sx={{display: 'flex'}}>
                    {info !== null && (
                        <ErrorLayout requestMessage={info}/>
                    )}
                    {info == null && (
                        <PlantUMLDiagram umlText={puml}/>
                    )}
                </Box>
            </Main>
        </Box>
    );

}