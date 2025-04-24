import AppBar from '@mui/material/AppBar';
import Container from '@mui/material/Container';
import Toolbar from '@mui/material/Toolbar';
import {IconButton, Typography} from "@mui/material";
import AppsIcon from '@mui/icons-material/Apps';

interface HeaderAppBarProps {
    open: boolean;
    handleDrawerChange(isOpen: boolean): void;
}

export default function HeaderAppBar({open, handleDrawerChange}: HeaderAppBarProps){
    return (
        <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
            <Container maxWidth="xl">
                <Toolbar disableGutters>
                    <IconButton onClick={() => handleDrawerChange(!open)}>
                        <AppsIcon
                            fontSize={"large"}
                        />
                    </IconButton>
                    <Typography
                        variant="h6"
                        noWrap
                        component="a"
                        sx={{
                            mr: 2,
                            display: { xs: 'none', md: 'flex' },
                            fontFamily: 'monospace',
                            fontWeight: 700,
                            letterSpacing: '.3rem',
                            color: 'inherit',
                            textDecoration: 'none',
                        }}
                    >
                        ARCHITECTURE GENERATING TOOL
                    </Typography>

                </Toolbar>
            </Container>
        </AppBar>
    );
}