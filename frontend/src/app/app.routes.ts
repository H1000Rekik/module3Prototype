import { Routes } from '@angular/router';
import { ArenaComponent } from './arena/arena.component';
import { AuthComponent } from './auth/auth.component';
import { LobbyComponent } from './lobby/lobby.component';
import { MatchComponent } from './match/match.component';

export const routes: Routes = [
    { path: '', redirectTo: '/auth', pathMatch: 'full' },
    { path: 'arena', component: ArenaComponent }, // old module 3 demo
    { path: 'auth', component: AuthComponent },
    { path: 'lobby', component: LobbyComponent },
    { path: 'match/:roomId', component: MatchComponent }
];
