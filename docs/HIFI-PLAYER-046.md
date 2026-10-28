# Player HiFi 0.4.6 — verifica locale

Intervento limitato al player, al relativo menu e alle didascalie delle griglie album. Nessuna modifica allo schema del database, alle credenziali o ai dati della raccolta.

- Palette carbone/night e colore scelto dall'utente nel player; comando centrale con angoli di 8dp, avanzamento sottile e azioni circolari contornate.
- Il comando rapido del timer cerca ora l'album corrente su RYM attraverso `RymLinks`. Quando l'album non è disponibile l'azione classica è disabilitata. Il timer rimane nel menu.
- Menu a tre colonne, con Cerca album su RYM, Cerca brano su RYM e Mostra album sempre ai primi tre posti. Le voci mancanti rimangono disabilitate; le azioni successive conservano le proprie condizioni.
- Volume multimediale del telefono, rappresentato da 0 a 100, con aggiornamento dai tasti fisici mentre il menu è aperto. Nessun guadagno oltre il 100% in questo controllo. I livelli seguono i passi effettivi di Android.
- Font digitale per scritte brevi latine; font del dispositivo per nomi lunghi e altri alfabeti.
- In verticale sul telefono, titolo e artista hanno tutta la larghezza. Subito sotto, prima dell'avanzamento, una riga allineata a sinistra contiene Coda, RYM, Salvato e Tre puntini, con cerchi da 48dp e spazi da 16dp. Le alternative compatte e orizzontali rimangono disponibili. La disposizione va verificata manualmente sul dispositivo.
- Le didascalie album mantengono lo spazio complessivo: quando il titolo usa una sola riga, l'artista inizia subito sotto e può usare due righe.

## Verifiche

La suite completa ha segnalato fallimenti in test di shortcut/flag upstream e operazioni su file nell'ambiente Windows; non è stata dichiarata superata. La prima selezione mirata ha superato 54 test: tipografia, navigazione HiFi, indirizzi e abbinamento RYM, nomi dei controlli, player compatto, ordine di riproduzione, righe album e scadenza timer. La prova iniziale sul dispositivo ha confermato l'avvio della 0.4.6 e la presenza di RYM e Mostra album in prima riga; il testo del volume era troncato ed è stato corretto. In seguito l'utente ha richiesto di mantenere la gerarchia bianco/ambra, usare il logo RYM e collegare il volume a quello del telefono; la build finale con questi cambi è riuscita e la selezione mirata ha superato 57 test, inclusi i tre test sulla conversione del volume. La firma dell'APK corrisponde alla copia debug già installata. Il telefono è stato ricollegato e l'installazione finale via USB è riuscita; l'impronta dell'APK installato corrisponde alla build verificata. Rimane la prova manuale dell'utente sulla sincronizzazione del volume e sull'interfaccia. L'utente preferisce eseguire personalmente le prove guidate.

La build debug usa il pacchetto `local.hifi.player.debug`. Per aggiornare il telefono è necessaria la stessa firma della copia installata. Nessuna pubblicazione GitHub automatica è prevista per questa prova.




## Finiture selezionabili del player

Il selettore “Stile sfondo del player” propone solo HiFi attuale (predefinito), Anni ’80 · Metallo nero, Anni ’80 · Nero satinato e Anni ’80 · Argento spazzolato. La preferenza `hifiPlayerFinish` è separata dal vecchio stile basato sulle copertine: le vecchie preferenze non vengono riscritte, e un’installazione esistente parte dallo stile attuale.

Le finiture sono disegnate con Compose: cornice, trama statica e quattro piccole viti decorative. Dopo la prova sul telefono, il grande pannello nero è stato rimosso: il materiale copre tutta la superficie, anche dietro testi e comandi. Solo la copertina ha una cornice incassata. La variante argento usa testi scuri e adatta la luminosità degli accenti per il contrasto; il pulsante centrale mantiene il colore generale. Il logo RYM usa la risorsa originale `rym_mark`, ricolorata come prima dalla palette generale, senza sostituirla con il simbolo semplificato dei mockup.

Nelle finiture anni ’80 i comandi ordinari hanno altezza 56dp e Play/Pausa 64dp in verticale (56dp in orizzontale); nello stile attuale restano le misure precedenti. Le icone di sistema passano a scure quando il player argento è aperto e vengono ripristinate all’uscita. Aggiunti controlli del contrasto con tutti i dodici accenti e della conservazione delle palette classica/night nelle finiture scure.

Il vetro del player resta disponibile nello stile attuale; nelle finiture opache è disabilitato senza cancellare la preferenza salvata. Le altre pagine, i dati dell’account e il mini-player mantengono l’impostazione corrente. Da verificare sul telefono: passaggio fra tutte le finiture e ritorno ad attuale, cambio colore generale, testo grande, orientamento e comandi del player.

Build delle finiture completata e installata via USB nella copia debug; impronta dell’APK sul dispositivo verificata. Ultima selezione: 32 test passati (tipografia, volume, layout compatto, RYM e ordine di riproduzione). La texture finale è stata ricompilata dopo la selezione. Verifica visiva e uso delle tre finiture sul telefono affidati all’utente; nessuna pubblicazione GitHub eseguita.

La correzione della superficie completa e delle proporzioni è stata compilata e installata via USB. Impronta dell’APK installato verificata; 34 test mirati passati, senza errori. Il confronto visivo della nuova versione sul telefono resta da completare con l’utente.

## Rifinitura del player e cambio copertina

- Texture ridotta a segmenti sottili e irregolari, senza righe continue su tutta la larghezza.
- Share Tech Mono, già incluso nel progetto, sostituisce il carattere puntinato nelle scritte brevi del player e nei comandi; nomi lunghi e alfabeti non supportati mantengono il font leggibile del dispositivo.
- Icone inferiori più piccole dentro aree interattive invariate. Il pulsante centrale mantiene le proporzioni già concordate.
- Nelle finiture anni ’80 in verticale, con un brano disponibile, la coda resta accessibile dal pulsante; la vecchia striscia di anteprima non riserva più due altezze di coda vuote. Si mantengono gli spazi di sicurezza del sistema.
- Su schermi sufficientemente alti e con testo non molto ingrandito, copertina e controlli usano una distribuzione verticale dedicata; le distanze libere si distribuiscono tra azioni, avanzamento e trasporto anziché accumularsi sotto i pulsanti. Le finestre piccole e il testo grande conservano il percorso adattivo.
- Nel cambio brano con gesto, le varianti anni ’80 hanno una sola cornice fissa. Le pagine delle copertine scorrono in un viewport quadrato ritagliato, senza spostare la cornice e senza comparire sul materiale esterno. La vista testi conserva il proprio layout.

Da provare manualmente: avanti/indietro dai pulsanti e con trascinamento, fine/inizio coda, apertura e chiusura coda, testo ingrandito e confronto dei tre materiali. La verifica visiva è eseguita dall’utente, come richiesto.

Rifinitura compilata e installata via USB; impronta dell’APK verificata sul dispositivo. Ultima selezione: 43 test passati senza errori, inclusi riserva della coda e stato di apertura. La prova visiva delle animazioni e della nuova distribuzione resta manuale.

## Revisione compatta dopo la prova visiva

La distribuzione con due spazi espandibili fra le righe è stata scartata. Si usa la disposizione inferiore compatta della vista testi: titolo/artista, quattro azioni, avanzamento, tempi e trasporto formano un unico blocco di altezza naturale. Lo spazio disponibile sopra è riservato alla copertina quadrata, dimensionata sul lato minore, con margini costanti. Il contenuto delle immagini usa esplicitamente ContentScale.Fit, senza tagli né deformazioni.

Nelle varianti anni ’80 il titolo può occupare due righe con ellissi; la riga artisti rimane singola e scorre orizzontalmente se necessario. Il carosello mantiene la cornice fissa e introduce 12dp di spazio scuro tra le pagine: a riposo la pagina corrente occupa esattamente il viewport e le vicine rimangono fuori. I metadati dipendono dal brano selezionato dal player, non dalla posizione intermedia del trascinamento. La vista testi e lo stile HiFi attuale conservano la propria disposizione.

## Estetica scelta dal riferimento delle 22:00

L’utente ha scelto nuovamente il carattere digitale Dot Gothic e la texture metallica più visibile della schermata Roedelius precedente. Ripristinati questi due aspetti, mantenendo dimensioni correnti, controlli compatti, titolo fino a due righe, icone ridotte e separazione di 12dp fra copertine nel viewport fisso. La riserva inferiore del player hardware in verticale aumenta di 8dp per spostare leggermente verso l’alto titolo e controlli senza modificare il rapporto delle immagini.

## Titolo ad altezza stabile

Nelle finiture hardware in verticale il titolo usa sempre due righe (minLines e maxLines entrambe a 2), anche se il testo è breve; i titoli più lunghi terminano con ellissi. La riga artista rimane singola. La riserva inferiore aumenta di altri 16dp rispetto alla revisione precedente, spostando più in alto il blocco compatto senza usare offset che sovrappongano i controlli alla copertina. Il confronto fra un titolo breve e uno lungo resta una prova visiva sul telefono.

## Ultima scelta: una riga per titolo e artista

La riserva di due righe è sostituita dalla richiesta successiva: titolo e artista hanno ciascuno una sola riga, vicine, senza padding aggiuntivo del font. I nomi lunghi scorrono orizzontalmente dopo una breve pausa. Le altezze di riga scalano con il testo di sistema. Il blocco inferiore riceve un ulteriore rialzo di 32dp rispetto alla revisione precedente (sostituisce la richiesta intermedia di 16dp). Cornice fissa e separazione fra copertine rimangono invariate.
