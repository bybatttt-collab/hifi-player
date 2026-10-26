# HiFi Player

Lettore Android sperimentale per YouTube Music e file locali. Fork indipendente di [InterTune](https://github.com/ItzSkyeYT/InterTune), revisione iniziale 0f300ee97246383f465e38e246ee609bae010050, GPL-3.0-only. Nessuna affiliazione con YouTube, Pear, InterTune o Rate Your Music.

## Versione 0.4.2

Introduzione HiFi per le nuove installazioni, in tre passaggi: aggiornamenti e notifiche, tema e colore, collegamento facoltativo a YouTube Music. Nessuna guida InterTune o serie di domande aggiuntive. Gli utenti già configurati non ripetono la procedura.

Home essenziale: playlist fissate, ultimi 24 album salvati e tre brani piaciuti recenti. Toccare uno di questi brani prepara la coda completa dei brani piaciuti, nell’ordinamento della sezione Brani. Nessun dado, suggerimento, nuova uscita o playlist della community. Circa tre copertine per volta.

Cinque schede: Home, Brani, Album, Playlist e RYM. Brani mostra i piaciuti; Album gli album salvati, senza filtri o riepiloghi. Playlist contiene le playlist e la cartella Brani scaricati. Strumenti, account e sincronizzazione sono nelle impostazioni. La navigazione delle schede torna alle rispettive pagine principali.

Tema hi-fi carbone e ambra e nuovo Dark / Night, con campioni colore selezionabili e font del dispositivo. Le vecchie voci tema dinamico e tema scuro sono rimosse; le altre opzioni restano disponibili. Nessuna barra laterale di scorrimento visibile. Menu di ordinamento allineati tra Album, Brani e Playlist. Titoli delle griglie allineati; copertine delle pagine playlist di misura fissa con testo affiancato. Nessun cuore o contrassegno sugli elementi salvati degli elenchi; il salvataggio usa il segnalibro. Uscita audio nei tre punti. Nessun numero sopra le copertine dei brani di un album.

## RYM

Browser incorporato con sessione propria, navigazione, zoom e blocco degli annunci attivo per impostazione predefinita. Il filtro usa domini e selettori EasyList: non garantisce il blocco di ogni annuncio e non filtra gli stream YouTube. Licenza e attribuzioni in third_party/easylist.

“Cerca su RYM” tenta il primo risultato pertinente e ripiega sulla ricerca visibile in caso di limiti del motore di ricerca. Nelle pagine album RYM: “Apri in HiFi” e “Salva in raccolta” usano il primo album trovato su YouTube Music. Se la ricerca album è vuota cercano su YouTube aggiungendo “full album”: apertura dei dettagli del video con riproduzione esplicita oppure salvataggio nella playlist “full album”. La playlist viene creata se manca; con scrittura account disattivata è locale. Le conferme di salvataggio scompaiono dopo circa 2,5 secondi. Gli errori restano visibili.

Nessuna password o sessione Chrome importata. L’utente accede personalmente sul sito. Nessuna interfaccia JavaScript con privilegi nativi esposta alle pagine. CAPTCHA e verifiche del sito richiedono intervento personale.

## Compilazione

JDK 21, Android SDK e build-tools 36, NDK 29.0.13113456 e CMake 3.31.6. Da un clone inizializzare i sottomoduli:

    git submodule update --init --recursive
    ./gradlew :app:assembleCoreDebug

Su Windows usare gradlew.bat. L’archivio sorgenti della release comprende già i sottomoduli. Impostare ANDROID_HOME oppure local.properties con il percorso SDK. Su Linux potrebbe essere necessario chmod +x gradlew.

Build arm64, Android 8 o successivo. Prove reali su Samsung A56 / Android 16. La copia debug usa local.hifi.player.debug; quella distribuita local.hifi.player. Hanno dati separati.

Per la release: :app:assembleCoreRelease. La firma personale usa HIFI_KEYSTORE_FILE, HIFI_STORE_PASSWORD, HIFI_KEY_PASSWORD e alias hifi-player, oppure keystore.properties locale escluso da Git. Senza queste informazioni la release è non firmata. Conservare la stessa chiave per gli aggiornamenti. Non pubblicare chiavi o password.

## Distribuzione e aggiornamenti

[Release pubbliche](https://github.com/bybatttt-collab/hifi-player/releases). Il primo APK può essere condiviso direttamente. Le successive versioni sono proposte nell’app, senza account GitHub. Durante la configurazione abilitare il controllo e consentire le notifiche; Android gestisce i tempi dei controlli in background. In Impostazioni → Aggiornamenti è disponibile il controllo manuale.

Asset: HiFiPlayer-<versione>-core-release-<versionCode>.apk. Ogni aggiornamento richiede un versionCode crescente e la stessa firma. Download limitato al repository configurato; verifica di pacchetto, versione e firma prima dell’installazione. Android richiede autorizzazione e conferma dell’utente. Nessuna installazione silenziosa. Il controllo è disattivato nelle build debug.

## Prove e limiti

Test JVM per navigazione, indirizzi RYM, primo risultato, domini pubblicitari, fonte degli aggiornamenti, decodifica e paginazione della raccolta. Prove sul dispositivo di avvio, ricerca, raccolta album, riproduzione e browser RYM. I test simulati non dimostrano la disponibilità permanente di YouTube Music o la correttezza di ogni account.

Scelta rigorosa della versione audio, cartelle locali e copertura completa delle mutazioni bidirezionali richiedono ancora sviluppo o prove. L’accesso non ufficiale a YouTube Music può cambiare. I caricamenti personali non sono importati automaticamente. La sincronizzazione album richiede esplicitamente Recently added di YouTube Music e ne conserva le posizioni attraverso tutte le pagine, indipendentemente dagli inserimenti nel database. Home e Album usano lo stesso ordine. I nuovi salvataggi locali precedono la copia sincronizzata fino al successivo aggiornamento completo. Le letture incomplete mantengono le posizioni precedenti. Le date storiche originali non sono disponibili: vengono conservate le posizioni senza inventare date. Dopo questo aggiornamento aprire Album per ricostruire automaticamente l’ordine.

APK e sorgenti corrispondenti, inclusi sottomoduli e licenze, sono pubblicati insieme. Conservare LICENSE e attribuzioni di InterTune, OuterTune, InnerTune e dipendenze. README-UPSTREAM.md conserva la documentazione della base e può descrivere funzioni o servizi non disponibili in questo fork.