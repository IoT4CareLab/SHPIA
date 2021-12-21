# Aggiornamenti
1. Applicazione in inglese
2. Nuovo sistema di raccolta dei dati con performance migliorate
3. Implementazione del multithreading durante la raccolta dati
4. Implementata interfaccia grafica per mostare i dati raccoli in tempo reale

# Problemi noti
1. In rare occasioni il telefono smette di ricevere aggiornamenti ma non perde la connessione con il sensore. (Rilevato solo con OP3T)
2. La stabilità della connessione è ancora da definire. Test con Huaweii a 50hz ha raccolto correttamente 500 dati ogni 10 secondi per 15 minuti a schermo spento
3. I sensori di enviroment sembrano avere problemi con la frequenza. Settandoli a 50hz si riescono a raccogliere 50 dati ogni 10 secondi anzichè 500. Aumentando a 100hz
   si riesce ad arrivare a 100 dati raccolti ma quando si prova ad incrementare ulteriormente le prestazione cominciano a diminuire (66 campionature a 150hz e 50 a 200hz).
   Da definire se quale sia il range di frequenza di questi sensori.
4. Collegando più sensori le prestazioni di raccolta dati del secondo scendono in modo inversamente proporzionale alla frequenza. A valori di 50hz la campionatura sembra
   procedere abbastanza correttamente ma sono necessarie ulteriori prove.
5. Le prestazioni della campionatura scendono in generale quando il telefono non è in idle. Azioni come lo sblocco dello schermo o la navigazione della UI influiscono
   sulla raccolta.


# Todo
1. Rework upload local storage quando il device torna online
2. Rimuovere vecchie strutture dati per la raccolta (NordicDeviceData ...)
3. provare motion e environment con frequenze basse. (motion 50hz, temperatura ad almeno 1hz)
4. stoppare service di raccolta quando tutti i sensori si sono disconnessi
5. migliorare first discovery aspettare le fine del service discovery
6. preference non si aggiornano quando si fa il wipe della cache. è necessario visitare la pagina delle impostazioni per attivarle


# Test dei limiti dei sensori (SHORT)
1. 40hz motion, 1hz env, 1 sensore, FUNZIONA 
2. 50hz motion, 1hz env, 1 sensore, CRASHA
3. 50hz motion, 0.5hz env, 1 sensore, FUNZIONA

4. 20hz motion, 0.5hz env, 2 sensori, FUNZIONA


# Nuovo
1. Bug crash del first login failed
2. Update sensor details
3. Rimuovere vecchie strutture e servizi
4. Impedire inizio di una nuova raccolta dati se ce ne è già una attiva
5. Se il bluethooth è disattivato la ricerca crasha
6. Ogni tanto la chiamata post fallisce. Probabilmente system.nanotime