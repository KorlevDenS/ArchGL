import React, {useState} from 'react';
import './App.css';
import HeaderAppBar from "./HeaderAppBar";
import ArchConstructor from "./ArchConstructor";

export default function App() {

    const [open, setOpen] = useState(true);

    const handleDrawerChange = (isOpen: boolean) => {
        setOpen(isOpen);
    };

    return (
        <div className="App">
            <div className="App-content">
                <HeaderAppBar handleDrawerChange={handleDrawerChange} open={open} ></HeaderAppBar>
                <ArchConstructor handleDrawerChange={handleDrawerChange} open={open}/>
            </div>
        </div>
    );
}