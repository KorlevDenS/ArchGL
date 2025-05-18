import {
    Autocomplete,
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
import {
    AcceptFrom,
    AcceptRequestFrom,
    Action,
    Actor,
    AppMust,
    Data,
    Generate,
    Process, ProcessObtaining,
    Read,
    ReadRelated, SendTo, SendToObtaining, Save, Update, Delete, SaveRelated, UpdateRelated, DeleteRelated, FR
} from "./Model";

class ArchGLProgram {
    program: string

    constructor(program: string) {
        this.program = program;
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

    const [puml, setPuml] = useState<string>('A -> B: Hello');
    const [info, setInfo] = useState<InfoResponse | null>(new InfoResponse(
        undefined, "Введите требования к вашему приложению")
    );

    const [usersNumber, setUsersNumber] = useState<number>(0);
    const [onlineUsersNumber, setOnlineUsersNumber] = useState<number>(0);

    const [faultTolerance, setFaultTolerance] = useState<boolean>(false);

    const [latency, setLatency] = useState<string>("not-important");
    const [availability, setAvailability] = useState<string>("not-important");


    const validateName = (name: string): boolean=>  {
        if (name === "") {
            setInfo(new InfoResponse(undefined, "Название объекта не должно быть пустым"));
            return false;
        }
        if (!/^[A-Z]/.test(name)) {
            setInfo(new InfoResponse(undefined, "Название объекта должно начинаться с заглавной латинской буквы"));
            return false;
        }
        if (!/^[A-Za-z0-9]+$/.test(name)) {
            setInfo(new InfoResponse(undefined, "Название объекта может содержать только латинские буквы или цифры"));
            return false;
        }
        if (chipActors.some(obj => obj.name === name) || chipData.some(obj => obj.name === name)) {
            setInfo(new InfoResponse(undefined, "Название объекта должно быть уникальным"));
            return false;
        }
        return true;
    }


    const [chipActors, setChipActors] = useState<Actor[]>([]);
    const handleDeleteActor = (chipToDelete: Actor) => () => {
        setChipActors((chips) => chips.filter((chip) => chip.name !== chipToDelete.name));
    };
    const handleAddActor = () => {
        if (!validateName(userName)) {
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
    const handleDeleteData = (chipToDelete: Data) => () => {
        setChipData((chips) => chips.filter((chip) => chip.name !== chipToDelete.name));
    };
    const handleAddData = () => {
        if (!validateName(dataName)) {
            return;
        }
        if (dataType === "") {
            setInfo(new InfoResponse(undefined, "Тип данных не должен быть пустым"));
            return;
        }
        setDataUnitVolume(Math.floor(dataUnitVolume));
        if (dataUnitVolume < 1) {
            setInfo(new InfoResponse(undefined, "Размер данных не должен быть меньше 1 байта"));
            return;
        }
        setDataRetention(Math.floor(dataRetention));
        if (dataRetention < 0) {
            setInfo(new InfoResponse(undefined, "Время хранения данных не должно быть меньше 0 мс."));
            return;
        }
        const newArr: Data[] = chipData.slice();
        newArr.push(new Data(dataName, dataType, dataRetention, dataUnitVolume));
        setChipData(newArr);
        setInfo(new InfoResponse(undefined, "Введите требования к вашему приложению"));
    }

    const [chipFrs, setChipFrs] = useState<FR[]>([]);
    const handleDeleteFr = (chipToDelete: FR) => () => {
        setChipFrs((chips) => chips.filter((chip) => chip.name !== chipToDelete.name));
    };
    const [actionsToChoose, setActionsToChoose] = useState<Action[]>([new AppMust()]);
    const [actions, setActions] = useState<Action[]>([]);

    const makeDataPairs = (ds: string[], as: string[]): string[][] =>  {
        const pairs: string[][] = [];
        if (ds.length > 0 && as.length > 0) {
            ds.forEach( d => {
                as.forEach( a => {
                    pairs.push([a, d]);
                    pairs.push([d, a]);
                });
            });
        }
        if (ds.length > 1) {
            ds.forEach( d => {
                ds.forEach( d1 => {
                    if (d !== d1) {
                        pairs.push([d, d1]);
                    }
                })
            });
        }
        if (as.length > 1) {
            as.forEach( a => {
                as.forEach( a1 => {
                    if (a !== a1) {
                        pairs.push([a, a1]);
                    }
                })
            });
        }
        return pairs;
    }

    const handleActionsInput = (newVal: Action[]) => {
        setActions(newVal);
        if (newVal.length === 0) {
            setActionsToChoose([new AppMust()])
        } else {
            const newChoice: Action[] = [];
            const lastChoice = newVal[newVal.length - 1];
            if (lastChoice instanceof AppMust) {
                chipActors.forEach( a => {
                    newChoice.push(new AcceptRequestFrom(a.name));
                    chipData.forEach(d => {
                       newChoice.push(new AcceptFrom(d.name, a.name));
                    });
                });
            } else if (lastChoice instanceof AcceptRequestFrom) {
                chipData.forEach( d => {
                    newChoice.push(new Generate(d.name));
                    newChoice.push(new Read(d.name));
                });
                chipActors.forEach( a => {
                    newChoice.push(new Generate(a.name));
                    newChoice.push(new Read(a.name));
                });
                makeDataPairs(chipData.map(d => d.name), chipActors.map(a => a.name)).forEach(pair => {
                    newChoice.push(new ReadRelated(pair[0], pair[1]));
                });
            } else {
                newChoice.push(new Process());
                newChoice.push(new Save());
                newChoice.push(new Update());
                newChoice.push(new Delete());

                chipData.forEach( d => {
                    newChoice.push(new ProcessObtaining(d.name));
                    newChoice.push(new SaveRelated(d.name));
                    newChoice.push(new UpdateRelated(d.name));
                    newChoice.push(new DeleteRelated(d.name));
                });
                chipActors.forEach( a => {
                    newChoice.push(new ProcessObtaining(a.name));
                    newChoice.push(new SendTo(a.name));
                    chipData.forEach( d => {
                        newChoice.push(new SendToObtaining(a.name, d.name));
                    });
                    newChoice.push(new SaveRelated(a.name));
                    newChoice.push(new UpdateRelated(a.name));
                    newChoice.push(new DeleteRelated(a.name));
                });
            }
            setActionsToChoose(newChoice);
        }
    }

    const handleAddFr = () => {
        if (!validateName(frName)) {
            return;
        }
        setFrFrequency(Math.floor(frFrequency));
        if (frFrequency < 1) {
            setInfo(new InfoResponse(undefined, "Количество запросов требования в день не может быть < 1"));
            return;
        }
        if (actions.length < 2) {
            setInfo(new InfoResponse(undefined, "Должно быть действие, принимающее запрос/данные"));
            return;
        }
        const newArr: FR[] = chipFrs.slice();
        newArr.push(new FR(frName, frFrequency, actions));
        setChipFrs(newArr);
        setActions([]);
        setActionsToChoose([new AppMust()])
        setInfo(new InfoResponse(undefined, "Введите требования к вашему приложению"));
    }

    const [userName, setUserName] = useState<string>("");
    const [userType, setUserType] = useState<string>("web-client");

    const [dataName, setDataName] = useState<string>("");
    const [dataType, setDataType] = useState<string>("structuredText");
    const [dataRetention, setDataRetention] = useState<number>(0);
    const [dataUnitVolume, setDataUnitVolume] = useState<number>(0);

    const [frName, setFrName] = useState<string>("");
    const [frFrequency, setFrFrequency] = useState<number>(0);


    const getGeneratedUml = async (dslCode: string) => {
        try {
            await fetch("http://localhost:8080/arch/gateway/generate", {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(new ArchGLProgram(dslCode)),
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

    const validateApp = (): boolean => {
        setUsersNumber(Math.floor(usersNumber));
        if (usersNumber < 1) {
            setInfo(new InfoResponse(undefined, "Количество пользователей не может быть < 1"));
            return false;
        }
        setOnlineUsersNumber(Math.floor(onlineUsersNumber));
        if (onlineUsersNumber < 1) {
            setInfo(new InfoResponse(undefined, "Количество одновременных пользователей не может быть < 1"));
            return false;
        }
        if (onlineUsersNumber > usersNumber) {
            setInfo(new InfoResponse(undefined, "Количество одновременных пользователей не " +
                "может быть больше общего их количества"));
            return false;
        }
        if (chipActors.length === 0) {
            setInfo(new InfoResponse(undefined, "Добавьте как минимум одного актора"));
            return false;
        }
        if (chipFrs.length === 0) {
            setInfo(new InfoResponse(undefined, "Добавьте как минимум одно функциональное требование"));
            return false;
        }
        return true;
    }

    const handleSubmit = () => {
        if (!validateApp()) {
            return;
        }
        let actorsCode = "";
        chipActors.forEach(a => {
            actorsCode += a.expression();
        });
        let dataCode = "";
        chipData.forEach(d => {dataCode += d.expression();});
        let frsCode = "";
        chipFrs.forEach(f => {frsCode += f.expression();});
        const prog = "" +
            "app My_App {\n" +
            "   \n" +
            "   usersNumber: " + usersNumber + "\n" +
            "   latency: \"" + latency + "\"\n" +
            "   onlineUsersNumber: " + onlineUsersNumber + "\n" +
            "   availability: \"" + availability + "\"\n" +
            "   faultTolerance: \"" + (faultTolerance ? "yes" : "no") + "\"\n" +
            "   \n" +
            actorsCode +
            "   \n" +
            dataCode +
            "   \n" +
            frsCode +
            "}\n";
        console.log(prog);
        setInfo(new InfoResponse(undefined, "Выполняется генерация"))
        getGeneratedUml(prog).then();
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
                    <TextField variant="standard" color={"primary"}
                               label="Общее предполпгаемое количество пользователей" type={"number"}
                               onChange={(e) => {setUsersNumber(Number(e.target.value))}}/>
                    <TextField variant="standard" color={"primary"}
                               label="Максимаьное количество одновренменных пользователей" type={"number"}
                               onChange={(e) => {setOnlineUsersNumber(Number(e.target.value))}}/>
                    <FormControl variant="standard" sx={{minWidth: 200 }}>
                        <InputLabel id="demo-simple-select-label">Латентность</InputLabel>
                        <Select
                            labelId="demo-simple-select-label"
                            id="demo-simple-select"
                            value={latency}
                            label="Латеннтность"
                            onChange={e => {setLatency(e.target.value)}}
                        >
                            <MenuItem value={"low"}>максимально снизить</MenuItem>
                            <MenuItem value={"middle"}>нормальная</MenuItem>
                            <MenuItem value={"not-important"}>не принципиально</MenuItem>
                        </Select>
                    </FormControl>
                    <FormControl variant="standard" sx={{minWidth: 200 }}>
                        <InputLabel id="demo-simple-select-label">Доступность</InputLabel>
                        <Select
                            labelId="demo-simple-select-label"
                            id="demo-simple-select"
                            value={availability}
                            label="Доступность"
                            onChange={e => {setAvailability(e.target.value)}}
                        >
                            <MenuItem value={"high"}>максимально повысить</MenuItem>
                            <MenuItem value={"middle"}>нормальная</MenuItem>
                            <MenuItem value={"not-important"}>не принципиально</MenuItem>
                        </Select>
                    </FormControl>
                    <FormControlLabel
                        labelPlacement="start"
                        label="Важна высокая отказоустойчивость"
                        control={
                            <Checkbox checked={faultTolerance} onChange={() => {setFaultTolerance(!faultTolerance)}} color="primary"/>
                        }
                    />
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
                                <MenuItem value={"web-client"}>веб-клиент</MenuItem>
                                <MenuItem value={"service"}>сервис</MenuItem>
                                <MenuItem value={"scheduler"}>планировщик задач</MenuItem>
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
                        Добавьте данные
                    </Typography>

                    <Box display="flex"
                         flexDirection="row"
                         alignItems="center"
                    >
                        <TextField sx={{mr: 9 }} variant="standard" color={"primary"}
                                   label="Название данных"
                                   onChange={(e) => {setDataName(e.target.value)}}/>
                        <FormControl variant="standard" sx={{minWidth: 200 }}>
                            <InputLabel id="demo-simple-select-label">Тип данных</InputLabel>
                            <Select
                                labelId="demo-simple-select-label"
                                id="demo-simple-select"
                                value={dataType}
                                label="Тип данных"
                                onChange={e => {setDataType(e.target.value)}}
                            >
                                <MenuItem value={"structuredText"}>структурированный текст</MenuItem>
                                <MenuItem value={"text"}>текст</MenuItem>
                                <MenuItem value={"number"}>число</MenuItem>
                                <MenuItem value={"html"}>html-страница</MenuItem>
                                <MenuItem value={"notification"}>уведомление</MenuItem>
                                <MenuItem value={"image"}>изображение</MenuItem>
                                <MenuItem value={"file"}>файл</MenuItem>
                                <MenuItem value={"video"}>видео файл</MenuItem>
                                <MenuItem value={"videoStream"}>поток видео</MenuItem>
                                <MenuItem value={"audio"}>аудио файл</MenuItem>
                                <MenuItem value={"audioStream"}>поток аудио</MenuItem>
                                <MenuItem value={"statusCode"}>код ответа</MenuItem>
                            </Select>
                        </FormControl>
                    </Box>
                    <Box display="flex"
                         flexDirection="row"
                         alignItems="center"
                    >
                        <TextField sx={{mr: 9 }} variant="standard" color={"primary"}
                                   label="Экземпляр в байтах" type={"number"}
                                   onChange={(e) => {setDataUnitVolume(Number(e.target.value))}}/>

                        <TextField variant="standard" color={"primary"}
                                   label="Время хранения в мс." type={"number"} sx={{minWidth: 205 }}
                                   onChange={(e) => {setDataRetention(Number(e.target.value))}}/>
                    </Box>
                    <Button variant="outlined" onClick={() => {handleAddData()}}>Добавить данные</Button>
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
                        {chipData.map((data) => {
                            return (
                                <ListItem sx={{width: "inherit", p: 0.5}} key={data.name}>
                                    <Chip
                                        sx={{
                                            height: 'auto',
                                            '& .MuiChip-label': {
                                                display: 'block',
                                                whiteSpace: 'normal',
                                            },
                                        }}
                                        label={data.toString()}
                                        onDelete={handleDeleteData(data)}
                                    />
                                </ListItem>
                            );
                        })}
                    </Paper>
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
                         Функциональные требования
                    </Typography>
                    <Box display="flex"
                         flexDirection="row"
                         alignItems="center"
                    >
                        <TextField sx={{mr: 9 }} variant="standard" color={"primary"}
                                   label="Название требования"
                                   onChange={(e) => {setFrName(e.target.value)}}/>
                        <TextField variant="standard" color={"primary"}
                                   label="Запросов в день" type={"number"} sx={{minWidth: 205 }}
                                   onChange={(e) => {setFrFrequency(Number(e.target.value))}}/>
                    </Box>
                    <Autocomplete
                        multiple
                        id="tags-standard"
                        value={actions}
                        onChange={(_event, newValue) => {
                            handleActionsInput(newValue)
                        }}
                        options={actionsToChoose}
                        getOptionLabel={(option) => option.description()}
                        renderValue={(values, getItemProps) =>
                            values.map((option, index) => {
                                const { key} = getItemProps({ index });
                                return (<Chip key={key} label={option.description()}/>);
                            })
                        }
                        renderInput={(params) => (
                            <TextField
                                {...params}
                                variant="standard"
                                label="Действия"
                            />
                        )}
                    />
                    <Button variant="outlined" onClick={() => {handleAddFr()}}>Добавить требование</Button>
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
                        {chipFrs.map((fr) => {
                            return (
                                <ListItem sx={{width: "inherit", p: 0.5}} key={fr.key}>
                                    <Chip
                                        sx={{
                                            height: 'auto',
                                            '& .MuiChip-label': {
                                                display: 'block',
                                                whiteSpace: 'normal',
                                            },
                                        }}
                                        label={fr.toString()}
                                        onDelete={handleDeleteFr(fr)}
                                    />
                                </ListItem>
                            );
                        })}
                    </Paper>
                    <Button variant="contained" onClick={() => {handleSubmit()}}>Сгенерировать</Button>
                    <Divider/>
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