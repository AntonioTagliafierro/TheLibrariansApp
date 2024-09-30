#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <time.h>
#include "/usr/include/postgresql/libpq-fe.h"

#define PORT 8080
#define BUFFER_SIZE 1024

pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;

// Funzione per gestire il client (registrazione o login)
void *handle_client(void *socket_desc);
void handle_register(PGconn *conn, const char *username, const char *password);
int handle_login(PGconn *conn, const char *username, const char *password);
void send_books_from_db(PGconn *conn, int sock);
void send_books_from_db_key_genre(PGconn *conn, int sock, const char *field1, const char *field2);
void send_books_from_db_key(PGconn *conn, int sock, const char *field1);
void send_books_from_db_genre(PGconn *conn, int sock, const char *field1);
void add_to_bag(PGconn *conn, int sock, const char *username, const char *isbn);
void rem_to_bag(PGconn *conn, int sock, const char *username, const char *isbn);
void books_from_bag(PGconn *conn, int sock, const char *username);
void num_prestiti(PGconn *conn, int sock, const char *username);
void ordina_book(PGconn *conn, int sock, const char *username, const char *isbn);
void send_books_from_db_isbn(PGconn *conn, int sock, const char *isbn);
int get_book_quantity(PGconn *conn, const char *isbn);
int get_book_copies_on_loan(PGconn *conn, const char *isbn);
void get_date(char *date_str, size_t max_size);
void get_delivery_date(char *buffer, size_t buffer_size);
void get_loans_by_isbn(PGconn *conn, int sock, const char *isbn);
void controllaPrestitiInRitardo(PGconn *conn, int sock, const char *username);
void get_loans_by_user(PGconn *conn, int sock, const char *username);
void update_loan_status(PGconn *conn, int sock, const char *isbn, const char *username);
void get_loans_by_isbn_not_delivered(PGconn *conn, int sock, const char *isbn);
void get_overdue_loans(PGconn *conn, int sock);
void get_overdue_loans_by_isbn(PGconn *conn, int sock, const char *isbn);

// Funzione per recuperare i prestiti attivi in ritardo di consegna per un dato ISBN
void get_overdue_loans_by_isbn(PGconn *conn, int sock, const char *isbn) {
    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));
        return;
    }

    // Esegui la query per prendere i prestiti attivi per un dato ISBN con data_fine superata rispetto alla data odierna
    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query),
             "SELECT username, isbn, data_inizio, data_fine "
             "FROM loan "
             "WHERE stato = 'attivato' AND isbn = '%s' AND data_fine < CURRENT_DATE;", isbn);
    
    PGresult *res = PQexec(conn, query);

    // Verifica se la query è andata a buon fine
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "SELECT failed: %s", PQerrorMessage(conn));
        PQclear(res);
        return;
    }

    int rows = PQntuples(res);
    char buffer[1024];

    // Verifica se ci sono prestiti in ritardo per l'ISBN specificato
    if (rows == 0) {
        snprintf(buffer, sizeof(buffer), "Nessun prestito in ritardo trovato per l'ISBN %s.\n", isbn);
        send(sock, buffer, strlen(buffer), 0);
    } else {
        // Itera sui risultati e invia i dati di ogni prestito in ritardo
        for (int i = 0; i < rows; i++) {
            snprintf(buffer, sizeof(buffer), "Utente: %s, ISBN: %s, Data Inizio: %s, Data Fine: %s (in ritardo)\n", 
                     PQgetvalue(res, i, 0),  // username
                     PQgetvalue(res, i, 1),  // isbn
                     PQgetvalue(res, i, 2),  // data_inizio
                     PQgetvalue(res, i, 3)); // data_fine

            send(sock, buffer, strlen(buffer), 0);
        }
    }

    // Invia segnale di fine ("END")
    send(sock, "END\n", 4, 0);

    PQclear(res);
}


// Funzione per recuperare i prestiti attivi in ritardo di consegna
void get_overdue_loans(PGconn *conn, int sock) {
    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));
        return;
    }

    // Esegui la query per prendere i prestiti attivi con data_fine superata rispetto alla data odierna
    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query),
             "SELECT username, isbn, data_inizio, data_fine "
             "FROM loan "
             "WHERE stato = 'attivato' AND data_fine < CURRENT_DATE;");
    
    PGresult *res = PQexec(conn, query);

    // Verifica se la query è andata a buon fine
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "SELECT failed: %s", PQerrorMessage(conn));
        PQclear(res);
        return;
    }

    int rows = PQntuples(res);
    char buffer[1024];

    // Verifica se ci sono prestiti in ritardo
    if (rows == 0) {
        snprintf(buffer, sizeof(buffer), "Non ci sono prestiti in ritardo.\n");
        send(sock, buffer, strlen(buffer), 0);
    } else {
        // Itera sui risultati e invia i dati di ogni prestito in ritardo
        for (int i = 0; i < rows; i++) {
            snprintf(buffer, sizeof(buffer), "Utente: %s, ISBN: %s, Data Inizio: %s, Data Fine: %s (in ritardo)\n", 
                     PQgetvalue(res, i, 0),  // username
                     PQgetvalue(res, i, 1),  // isbn
                     PQgetvalue(res, i, 2),  // data_inizio
                     PQgetvalue(res, i, 3)); // data_fine

            send(sock, buffer, strlen(buffer), 0);
        }
    }

    // Invia segnale di fine ("END")
    send(sock, "END\n", 4, 0);

    PQclear(res);
}


// Funzione per recuperare i prestiti associati a un determinato ISBN ancora attivi

void get_loans_by_isbn_not_delivered(PGconn *conn, int sock, const char *isbn) {
    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));
        return;
    }

    // Esegui la query per prendere i dettagli dei prestiti
    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query),
             "SELECT username, data_inizio, data_fine, stato "
             "FROM loan "
             "WHERE isbn = '%s' AND stato = 'attivato';", isbn);
    
    PGresult *res = PQexec(conn, query);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "SELECT failed: %s", PQerrorMessage(conn));
        PQclear(res);
        return;
    }

    int rows = PQntuples(res);
    char buffer[1024];

    // Verifica se ci sono prestiti
    if (rows == 0) {
        snprintf(buffer, sizeof(buffer), "Nessun prestito trovato per l'ISBN %s\n", isbn);
        send(sock, buffer, strlen(buffer), 0);
    } else {
        // Itera sui risultati e invia i dati di ogni prestito
        for (int i = 0; i < rows; i++) {
            snprintf(buffer, sizeof(buffer), "%s,%s,%s,%s\n", 
                     PQgetvalue(res, i, 0),  // username
                     PQgetvalue(res, i, 1),  // data_inizio
                     PQgetvalue(res, i, 2),  // data_fine
                     PQgetvalue(res, i, 3)); // stato

            send(sock, buffer, strlen(buffer), 0);
        }
    }

    // Invia segnale di fine ("END")
    send(sock, "END\n", 4, 0);

    PQclear(res);
}

// Funzione per cambiare lo stato di un prestito da 'attivato' a 'consegnato'
void update_loan_status(PGconn *conn, int sock, const char *isbn, const char *username) {
    // Controlla la connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));
        return;
    }

    // Prepara la query SQL per aggiornare lo stato del prestito
    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query),
             "UPDATE loan "
             "SET stato = 'consegnato' "
             "WHERE isbn = '%s' AND username = '%s' AND stato = 'attivato';", isbn, username);

    // Esegui la query
    PGresult *res = PQexec(conn, query);

    // Verifica se l'aggiornamento è andato a buon fine
    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        fprintf(stderr, "UPDATE failed: %s", PQerrorMessage(conn));
        PQclear(res);
        send(sock, "Errore nell'aggiornamento dello stato\n", 38, 0);
        return;
    }

    // Controlla se è stato aggiornato almeno un record
    if (PQcmdTuples(res) == 0) {
        send(sock, "Nessun prestito aggiornato (prestito non trovato o già consegnato)\n", 64, 0);
    } else {
        send(sock, "Stato del prestito aggiornato con successo a 'consegnato'\n", 57, 0);
    }

    // Libera la memoria del risultato
    PQclear(res);

    // Invia segnale di fine ("END")
    send(sock, "END\n", 4, 0);
}

// Funzione per recuperare i prestiti di un determinato utente
void get_loans_by_user(PGconn *conn, int sock, const char *username) {
    // Controlla la connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));
        return;
    }

    // Esegui la query per prendere i prestiti dell'utente
    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query),
             "SELECT isbn, data_inizio, data_fine, stato "
             "FROM loan "
             "WHERE username = '%s';", username);
    PGresult *res = PQexec(conn, query);

    // Verifica se la query è andata a buon fine
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "SELECT failed: %s", PQerrorMessage(conn));
        PQclear(res);
        return;
    }

    int rows = PQntuples(res);
    char buffer[1024];

    // Controlla se l'utente ha prestiti
    if (rows == 0) {
        snprintf(buffer, sizeof(buffer), "Nessun prestito trovato per l'utente %s\n", username);
        send(sock, buffer, strlen(buffer), 0);
    } else {
        // Itera sui risultati e invia i dettagli di ogni prestito
        for (int i = 0; i < rows; i++) {
            snprintf(buffer, sizeof(buffer), "%s,%s,%s,%s\n", 
                     PQgetvalue(res, i, 0),  // isbn
                     PQgetvalue(res, i, 1),  // data_inizio
                     PQgetvalue(res, i, 2),  // data_fine
                     PQgetvalue(res, i, 3)); // stato

            send(sock, buffer, strlen(buffer), 0);
        }
    }

    // Invia segnale di fine ("END")
    send(sock, "END\n", 4, 0);

    // Pulisci il risultato della query
    PQclear(res);
}


// Funzione per controllare i prestiti in ritardo
void controllaPrestitiInRitardo(PGconn *conn, int sock, const char *username) {
    PGresult *res;
    int numPrestitiInRitardo = 0;
    time_t dataOdierna = time(NULL);
    struct tm *tmDataOdierna = localtime(&dataOdierna);

    // Query per ottenere la data di fine dei prestiti
    char query[256];
    snprintf(query, sizeof(query),
             "SELECT dataFine FROM Prestiti WHERE username = '%s'", username);

    res = PQexec(conn, query);
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "Errore nella query: %s", PQerrorMessage(conn));
        PQclear(res);
        return;
    }

    int numRows = PQntuples(res);
    for (int i = 0; i < numRows; i++) {
        // Converte la stringa in un oggetto time_t
        char *dataFineStr = PQgetvalue(res, i, 0);
        struct tm tmDataFine;
        strptime(dataFineStr, "%Y-%m-%d", &tmDataFine);
        time_t dataFine = mktime(&tmDataFine);

        // Controlla se la dataFine è passata
        if (difftime(dataFine, dataOdierna) < 0) {
            numPrestitiInRitardo++;
        }
    }

    PQclear(res);

    // Invia messaggio al client se ci sono prestiti in ritardo
    if (numPrestitiInRitardo > 0) {
        char messaggio[100];
        snprintf(messaggio, sizeof(messaggio), "Hai %d consegne in ritardo", numPrestitiInRitardo);
        send(sock, messaggio, strlen(messaggio), 0);
    } else {
        char messaggio[100];
        snprintf(messaggio, sizeof(messaggio), "Non hai consegne in ritardo");
        send(sock, messaggio, strlen(messaggio), 0);
    }
}

// Funzione per recuperare i prestiti associati a un determinato ISBN
void get_loans_by_isbn(PGconn *conn, int sock, const char *isbn) {
    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));
        return;
    }

    // Esegui la query per prendere i dettagli dei prestiti
    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query),
             "SELECT username, data_inizio, data_fine, stato "
             "FROM loan "
             "WHERE isbn = '%s';", isbn);
    PGresult *res = PQexec(conn, query);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "SELECT failed: %s", PQerrorMessage(conn));
        PQclear(res);
        return;
    }

    int rows = PQntuples(res);
    char buffer[1024];

    // Verifica se ci sono prestiti
    if (rows == 0) {
        snprintf(buffer, sizeof(buffer), "Nessun prestito trovato per l'ISBN %s\n", isbn);
        send(sock, buffer, strlen(buffer), 0);
    } else {
        // Itera sui risultati e invia i dati di ogni prestito
        for (int i = 0; i < rows; i++) {
            snprintf(buffer, sizeof(buffer), "%s,%s,%s,%s\n", 
                     PQgetvalue(res, i, 0),  // username
                     PQgetvalue(res, i, 1),  // data_inizio
                     PQgetvalue(res, i, 2),  // data_fine
                     PQgetvalue(res, i, 3)); // stato

            send(sock, buffer, strlen(buffer), 0);
        }
    }

    // Invia segnale di fine ("END")
    send(sock, "END\n", 4, 0);

    PQclear(res);
}


// Funzione che ritorna la data odierna + 3 giorni in formato stringa
void get_delivery_date(char *buffer, size_t buffer_size) {
    time_t now = time(NULL);         // Ottiene il timestamp corrente
    now += 3 * 24 * 60 * 60;         // Aggiunge 3 giorni (in secondi)

    struct tm *tm_info = localtime(&now);  // Converte il timestamp in una struttura tm
    strftime(buffer, buffer_size, "%Y-%m-%d", tm_info);  // Formatta la data in 'YYYY-MM-DD'
}

// Funzione per ottenere la data odierna come stringa in formato YYYY-MM-DD
void get_date(char *date_str, size_t max_size) {
    time_t t = time(NULL);
    struct tm *tm_info = localtime(&t);

    // Formattazione della data in "YYYY-MM-DD"
    strftime(date_str, max_size, "%Y-%m-%d", tm_info);
}

// Funzione che restituisce la quantità di un libro
int get_book_quantity(PGconn *conn, const char *isbn) {
    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));
        return -1; // Restituisce -1 in caso di errore
    }

    char query[256];
    snprintf(query, sizeof(query), "SELECT quantita FROM books WHERE isbn = '%s';", isbn);

    // Esecuzione della query
    PGresult *res = PQexec(conn, query);
    
    // Verifica se la query è andata a buon fine
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        printf("Errore nell'esecuzione della query: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return -1; // Restituisce -1 in caso di errore
    }

    // Estrai il risultato della query
    int quantita = 0;
    if (PQntuples(res) > 0) {
        quantita = atoi(PQgetvalue(res, 0, 0)); // Primo campo: 'quantita'
    } else {
        printf("Nessun libro trovato con ISBN: %s\n", isbn);
    }

    // Libera la memoria del risultato
    PQclear(res);
    return quantita; // Restituisce la quantità
}

// Funzione che restituisce le copie prestate di un libro
int get_book_copies_on_loan(PGconn *conn, const char *isbn) {
    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));
        return -1; // Restituisce -1 in caso di errore
    }

    char query[256];
    snprintf(query, sizeof(query), "SELECT copieprestate FROM books WHERE isbn = '%s';", isbn);

    // Esecuzione della query
    PGresult *res = PQexec(conn, query);
    
    // Verifica se la query è andata a buon fine
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        printf("Errore nell'esecuzione della query: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return -1; // Restituisce -1 in caso di errore
    }

    // Estrai il risultato della query
    int copieprestate = 0;
    if (PQntuples(res) > 0) {
        copieprestate = atoi(PQgetvalue(res, 0, 0)); // Primo campo: 'copieprestate'
    } else {
        printf("Nessun libro trovato con ISBN: %s\n", isbn);
    }

    // Libera la memoria del risultato
    PQclear(res);
    return copieprestate; // Restituisce le copie prestate
}


//funzione per creare prestito

void ordina_book(PGconn *conn, int sock, const char *username, const char *isbn){

    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));

        return;
    }

    // Ottenere le informazioni del libro
    int quantita = get_book_quantity(conn, isbn);
    int copieprestate = get_book_copies_on_loan(conn, isbn);

    int copie_disponibili = quantita - copieprestate;

    if (copie_disponibili < 1){
        char response[128];
        snprintf(response, sizeof(response), "il libro con isbn %s e' terminato", isbn);
        send(sock, response, strlen(response), 0);

    } else {
        // Esegui la query 
        char query1[BUFFER_SIZE];
        char start_date[11];  // Spazio per "YYYY-MM-DD" + terminatore null
        char delivery_date[11];  // Buffer per la data formattata
        get_delivery_date(delivery_date, sizeof(delivery_date));
        get_date(start_date, sizeof(start_date));

        snprintf(query1, sizeof(query1), "DELETE FROM carrello WHERE isbn = %s AND username = '%s';", isbn, username);
        PGresult *res = PQexec(conn, query1);

        if (PQresultStatus(res) != PGRES_COMMAND_OK) {
            fprintf(stderr, "Errore durante la rimozione dal carrello: %s\n", PQerrorMessage(conn));
        } else {
            printf("Rimosso libro: %s per l'utente: %s dal carrello\n", isbn, username);
        }

        PQclear(res);

        char query2[BUFFER_SIZE];
        snprintf(query2, sizeof(query2), "INSERT INTO loan (username, isbn, data_inizio, data_fine, stato) VALUES ('%s', '%s', '%s', '%s', 'attivo');", username, isbn, start_date, delivery_date);
        PGresult *res2 = PQexec(conn, query2);

        if (PQresultStatus(res2) != PGRES_COMMAND_OK) {
            fprintf(stderr, "Errore durante la creazione del prestito: %s\n", PQerrorMessage(conn));
        } else {
            printf("Inserito prestito del libro: %s per l'utente: %s \n", isbn, username);
        }

        PQclear(res2);

        char response[128];
        snprintf(response, sizeof(response), "Inserito prestito del libro: %s per l'utente: %s \n", isbn, username);
        send(sock, response, strlen(response), 0);


    }

}

//funzione per recuperare il numero di prestiti all'attivo di un utente

void num_prestiti(PGconn *conn, int sock, const char *username){
    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));

        return;
    }

    // Esegui la query 
    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query), 
    "SELECT COUNT(*) "
    "FROM loan "
    "WHERE username = '%s' "
    "AND stato = 'attivo';", 
    username);
    PGresult *res = PQexec(conn, query);

    if (PQresultStatus(res) == PGRES_TUPLES_OK) {
        // Ottieni il numero di prestiti attivi
        int num_prestiti_attivi = atoi(PQgetvalue(res, 0, 0));
        printf("Numero di prestiti attivi per %s: %d\n", username, num_prestiti_attivi);

        // Prepara la risposta da inviare al client
        char response[256];
        snprintf(response, sizeof(response), "%d", num_prestiti_attivi);

        // Invia la risposta al client
        send(sock, response, strlen(response), 0);
    } else {
        // In caso di errore nella query
        fprintf(stderr, "Errore nell'esecuzione della query: %s\n", PQerrorMessage(conn));
        send(sock, "Errore durante l'esecuzione della query", 41, 0);
    }

    PQclear(res);

}

//funzione per recuperare i libri dal carrello
void books_from_bag(PGconn *conn, int sock, const char *username) {
    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));

        return;
    }

    // Esegui la query per prendere i dettagli dei libri
    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query), "SELECT b.isbn, b.titolo, b.genere, b.imageUrl, b.autore, b.quantita, b.copieprestate "
        "FROM books b "
        "JOIN bag ba ON b.isbn = ba.isbn "
        "WHERE ba.username = '%s';", username);
    PGresult *res = PQexec(conn, query);
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "SELECT failed: %s", PQerrorMessage(conn));
        PQclear(res);

        return;
    }

    int rows = PQntuples(res);
    char buffer[1024];
    
    // Itera sui risultati e invia i dati di ogni libro
    for (int i = 0; i < rows; i++) {
        snprintf(buffer, sizeof(buffer), "%s,%s,%s,%s,%s,%s,%s\n", 
                 PQgetvalue(res, i, 0),  // isbn
                 PQgetvalue(res, i, 1),  // titolo
                 PQgetvalue(res, i, 2),  // genere
                 PQgetvalue(res, i, 3),  // imageUrl
                 PQgetvalue(res, i, 4),  // autore
                 PQgetvalue(res, i, 5),  // quantita
                 PQgetvalue(res, i, 6)); // copieprestate

        send(sock, buffer, strlen(buffer), 0);
    }

    // Invia segnale di fine ("END")
    send(sock, "END\n", 4, 0);

    PQclear(res);

}


//funzione per rimuovere dal carrello
void rem_to_bag(PGconn *conn, int sock, const char *username, const char *isbn){

    printf("Richiesta di rimozione ricevuta per l'utente: %s, ISBN: %s\n", username, isbn);
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Errore connessione al database: %s\n", PQerrorMessage(conn));
        PQfinish(conn);
        return;
    }

    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query), "DELETE FROM bag WHERE username = '%s' AND isbn = '%s';", username, isbn);
    PGresult *res = PQexec(conn, query);

    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        fprintf(stderr, "Errore durante la rimozione dal database: %s\n", PQerrorMessage(conn));
    } else {
        printf("Rimosso libro: %s per l'utente: %s\n", isbn, username);
    }

    PQclear(res);


}

//funzione per aggiungere al carrello
void add_to_bag(PGconn *conn, int sock, const char *username, const char *isbn){

    printf("Richiesta di registrazione ricevuta per l'utente: %s\n", username);
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Errore connessione al database: %s\n", PQerrorMessage(conn));
        PQfinish(conn);
        return;
    }

    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query), "INSERT INTO bag (username, isbn) VALUES ('%s', '%s');", username, isbn);
    PGresult *res = PQexec(conn, query);

    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        fprintf(stderr, "Errore durante l'inserimento nel database: %s\n", PQerrorMessage(conn));
    } else {
        printf("Aggiunto libro:%s  per l'utente: %s\n",isbn, username);
    }

    PQclear(res);


}
// Funzione per gestire la registrazione
void handle_register(PGconn *conn,const char *username, const char *password) {
    printf("Richiesta di registrazione ricevuta per l'utente: %s\n", username);
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Errore connessione al database: %s\n", PQerrorMessage(conn));
        PQfinish(conn);
        return;
    }

    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query), "INSERT INTO users (username, password) VALUES ('%s', '%s');", username, password);
    PGresult *res = PQexec(conn, query);

    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        fprintf(stderr, "Errore durante l'inserimento nel database: %s\n", PQerrorMessage(conn));
    } else {
        printf("Registrazione completata per l'utente: %s\n", username);
    }

    PQclear(res);

}

// Funzione per gestire il login
int handle_login(PGconn *conn,const char *username, const char *password) {

    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Errore connessione al database: %s\n", PQerrorMessage(conn));
        PQfinish(conn);
        return 0;
    }

    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query), "SELECT * FROM users WHERE username = '%s' AND password = '%s';", username, password);
    PGresult *res = PQexec(conn, query);

    int login_success = (PQntuples(res) == 1);  // Se c'è una riga, il login è valido

    if (login_success) {
        printf("Login riuscito per l'utente: %s\n", username);
    } else {
        printf("Login fallito per l'utente: %s\n", username);
    }

    PQclear(res);


    return login_success;
}

//Funzione per richiedere tutti i libri
void send_books_from_db(PGconn *conn, int sock) {
    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));

        return;
    }

    // Esegui la query per prendere i dettagli dei libri
    PGresult *res = PQexec(conn, "SELECT isbn, titolo, genere, imageUrl, autore, quantita, copieprestate FROM books");
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "SELECT failed: %s", PQerrorMessage(conn));
        PQclear(res);

        return;
    }

    int rows = PQntuples(res);
    char buffer[1024];
    
    // Itera sui risultati e invia i dati di ogni libro
    for (int i = 0; i < rows; i++) {
        snprintf(buffer, sizeof(buffer), "%s,%s,%s,%s,%s,%s,%s\n", 
                 PQgetvalue(res, i, 0),  // isbn
                 PQgetvalue(res, i, 1),  // titolo
                 PQgetvalue(res, i, 2),  // genere
                 PQgetvalue(res, i, 3),  // imageUrl
                 PQgetvalue(res, i, 4),  // autore
                 PQgetvalue(res, i, 5),  // quantita
                 PQgetvalue(res, i, 6)); // copieprestate

        send(sock, buffer, strlen(buffer), 0);
    }

    // Invia segnale di fine ("END")
    send(sock, "END\n", 4, 0);

    PQclear(res);

}

//Funzione per richiedere libri doppio filtro
void send_books_from_db_key_genre(PGconn *conn, int sock, const char *field1, const char *field2) {
    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));

        return;
    }

    // Esegui la query per prendere i dettagli dei libri
    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query), 
    "SELECT isbn, titolo, genere, imageUrl, autore, quantita, copieprestate FROM books WHERE titolo LIKE '%%%s%%' AND genere = '%s'", 
    field1, field2);
    PGresult *res = PQexec(conn, "SELECT isbn, titolo, genere, imageUrl, autore, quantita, copieprestate FROM books");
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "SELECT failed: %s", PQerrorMessage(conn));
        PQclear(res);

        return;
    }

    int rows = PQntuples(res);
    char buffer[1024];
    
    // Itera sui risultati e invia i dati di ogni libro
    for (int i = 0; i < rows; i++) {
        snprintf(buffer, sizeof(buffer), "%s,%s,%s,%s,%s,%s,%s\n", 
                 PQgetvalue(res, i, 0),  // isbn
                 PQgetvalue(res, i, 1),  // titolo
                 PQgetvalue(res, i, 2),  // genere
                 PQgetvalue(res, i, 3),  // imageUrl
                 PQgetvalue(res, i, 4),  // autore
                 PQgetvalue(res, i, 5),  // quantita
                 PQgetvalue(res, i, 6)); // copieprestate

        send(sock, buffer, strlen(buffer), 0);
    }

    // Invia segnale di fine ("END")
    send(sock, "END\n", 4, 0);

    PQclear(res);

}

//Funzione per richiedere libri con keyword
void send_books_from_db_key(PGconn *conn, int sock, const char *field1) {
    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));

        return;
    }

    // Esegui la query per prendere i dettagli dei libri
    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query), "SELECT isbn, titolo, genere, imageUrl, autore, quantita, copieprestate FROM books WHERE titolo LIKE '%%%s%%'", field1);
    PGresult *res = PQexec(conn, query);
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "SELECT failed: %s", PQerrorMessage(conn));
        PQclear(res);

        return;
    }

    int rows = PQntuples(res);
    char buffer[1024];
    
    // Itera sui risultati e invia i dati di ogni libro
    for (int i = 0; i < rows; i++) {
        snprintf(buffer, sizeof(buffer), "%s,%s,%s,%s,%s,%s,%s\n", 
                 PQgetvalue(res, i, 0),  // isbn
                 PQgetvalue(res, i, 1),  // titolo
                 PQgetvalue(res, i, 2),  // genere
                 PQgetvalue(res, i, 3),  // imageUrl
                 PQgetvalue(res, i, 4),  // autore
                 PQgetvalue(res, i, 5),  // quantita
                 PQgetvalue(res, i, 6)); // copieprestate

        send(sock, buffer, strlen(buffer), 0);
    }

    // Invia segnale di fine ("END")
    send(sock, "END\n", 4, 0);

    PQclear(res);

}

//Funzione per richiedere libri con genere
void send_books_from_db_genre(PGconn *conn, int sock, const char *field1) {
    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));

        return;
    }

    // Esegui la query per prendere i dettagli dei libri
    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query), "SELECT isbn, titolo, genere, imageUrl, autore, quantita, copieprestate FROM books WHERE genere = '%s'", field1);
    PGresult *res = PQexec(conn, query);
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "SELECT failed: %s", PQerrorMessage(conn));
        PQclear(res);

        return;
    }

    int rows = PQntuples(res);
    char buffer[1024];
    
    // Itera sui risultati e invia i dati di ogni libro
    for (int i = 0; i < rows; i++) {
        snprintf(buffer, sizeof(buffer), "%s,%s,%s,%s,%s,%s,%s\n", 
                 PQgetvalue(res, i, 0),  // isbn
                 PQgetvalue(res, i, 1),  // titolo
                 PQgetvalue(res, i, 2),  // genere
                 PQgetvalue(res, i, 3),  // imageUrl
                 PQgetvalue(res, i, 4),  // autore
                 PQgetvalue(res, i, 5),  // quantita
                 PQgetvalue(res, i, 6)); // copieprestate

        send(sock, buffer, strlen(buffer), 0);
    }

    // Invia segnale di fine ("END")
    send(sock, "END\n", 4, 0);

    PQclear(res);

}
//Funzione per richiedere libri con isbn
void send_books_from_db_isbn(PGconn *conn, int sock, const char *isbn) {
    // Connessione a PostgreSQL
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(conn));

        return;
    }

    // Esegui la query per prendere i dettagli dei libri
    char query[BUFFER_SIZE];
    snprintf(query, sizeof(query), "SELECT isbn, titolo, genere, imageUrl, autore, quantita, copieprestate FROM books WHERE isbn = '%s'", isbn);
    PGresult *res = PQexec(conn, query);
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        fprintf(stderr, "SELECT failed: %s", PQerrorMessage(conn));
        PQclear(res);

        return;
    }

    int rows = PQntuples(res);
    char buffer[1024];
    
    // Itera sui risultati e invia i dati di ogni libro
    for (int i = 0; i < rows; i++) {
        snprintf(buffer, sizeof(buffer), "%s,%s,%s,%s,%s,%s,%s\n", 
                 PQgetvalue(res, i, 0),  // isbn
                 PQgetvalue(res, i, 1),  // titolo
                 PQgetvalue(res, i, 2),  // genere
                 PQgetvalue(res, i, 3),  // imageUrl
                 PQgetvalue(res, i, 4),  // autore
                 PQgetvalue(res, i, 5),  // quantita
                 PQgetvalue(res, i, 6)); // copieprestate

        send(sock, buffer, strlen(buffer), 0);
    }

    // Invia segnale di fine ("END")
    send(sock, "END\n", 4, 0);

    PQclear(res);

}


// Funzione eseguita da ogni thread per gestire il client
void *handle_client(void *socket_desc) {

    PGconn *conn = PQconnectdb("host=database port=5432 dbname=library user=postgres password=12345");

    int sock = *(int*)socket_desc;
    char buffer[BUFFER_SIZE] = {0};
    free(socket_desc);  // Liberiamo la memoria allocata per il socket

    // Leggi i dati inviati dal client
    int read_size = read(sock, buffer, BUFFER_SIZE);
    if (read_size <= 0) {
        printf("Errore durante la lettura dal client\n");
        close(sock);
        return NULL;
    }

    // Rimuovi il carattere di fine linea '\n' se presente
    char *newline_pos = strchr(buffer, '\n');
    if (newline_pos != NULL) {
        *newline_pos = '\0'; // Sostituisci '\n' con il terminatore di stringa '\0'
    }
    
    printf("Dati ricevuti: %s\n", buffer);

    // Estrai il tipo di richiesta (login o registrazione) e le credenziali
    char *token = strtok(buffer, ":");
    if (token != NULL) {
        char *request_type = token;
        char *field1 = strtok(NULL, ":");
        char *field2 = strtok(NULL, ":");

        //Blocco registrazione e login
        
            if (strcmp(request_type, "register") == 0) {
                // Gestione registrazione
                handle_register(conn, field1, field2);
                char *response = "Registrazione completata con successo!";
                send(sock, response, strlen(response), 0);
            } else if (strcmp(request_type, "login") == 0) {
                // Gestione login
                if (handle_login(conn, field1, field2)) {
                    char *response = "Login avvenuto con successo!";
                    send(sock, response, strlen(response), 0);
                } else {
                    char *response = "Login fallito: credenziali errate.";
                    send(sock, response, strlen(response), 0);
                }
                //blocco richieste libri
            } else if (strcmp(request_type, "allbooks") == 0){
                //gestione richiesta tutti i libri 
                send_books_from_db(conn, sock);
            } else if (strcmp(request_type, "isbn") == 0){
                //gestione richiesta tutti i libri 
                send_books_from_db_isbn(conn, sock, field1);
            } else if (strcmp(request_type, "totalfilter") == 0){
                //libri doppio filtro
                send_books_from_db_key_genre(conn, sock, field1, field2);
            } else if (strcmp(request_type, "onlytitle") == 0){
                //libri solo keyword
                send_books_from_db_key(conn, sock, field1);
            } else if (strcmp(request_type, "onlygenre") == 0){
                //libri solo genere
                send_books_from_db_genre(conn, sock, field1);
            } else if (strcmp(request_type, "aggiungialcarrello") == 0) {
                //aggiunge libro al carrello utente
                add_to_bag(conn, sock, field1, field2);
                char *response = "Aggiunto libro:%s  per l'utente: %s\n",field1, field2;
                send(sock, response, strlen(response), 0);
            } else if (strcmp(request_type, "rimuovidalcarrello") == 0){
                //aggiunge libro al carrello utente
                rem_to_bag(conn, sock, field1, field2);
                char *response = "Rimosso libro:%s  per l'utente: %s\n",field1, field2;
                send(sock, response, strlen(response), 0);
            } else if (strcmp(request_type, "bagbooks") == 0){
                //recupera libri dal carrello dell'utente
                books_from_bag(conn, sock, field1);
            } else if (strcmp(request_type, "numprestiti") == 0){
                //restituisce il numero di prestiti al'attivo del'utente
                num_prestiti(conn, sock, field1);

            } else if (strcmp(request_type, "ordina") == 0){
                //creare prestito con blocco
                pthread_mutex_init(&lock, NULL);
                pthread_mutex_lock(&lock);
                ordina_book(conn, sock, field1, field2);
                pthread_mutex_unlock(&lock);


            } else if (strcmp(request_type,"loansbyisbn") == 0){
                //prendere prestiti legati ad un libro
                get_loans_by_isbn(conn, sock, field1);

            } else if (strcmp(request_type,"checkloans") == 0) {
                //controlla se ci sono prestiti non consegnati quando la data di scadenza è passata
                controllaPrestitiInRitardo(conn, sock, field1);

            } else if (strcmp(request_type,"userloans") == 0) {
                //recupera i prestiti di un utente
                get_loans_by_user(conn, sock, field1);
            } else if (strcmp(request_type,"delivered") == 0) {
                //imposta lo stato di un prestito a consegnato
                update_loan_status(conn, sock, field1, field2);
            }else if (strcmp(request_type,"loansbyisbnnotdelivered") == 0) {
                //recupera i prestiti di un dato isbn ancora attivi
                get_loans_by_isbn_not_delivered(conn, sock, field1);
            } else if (strcmp(request_type,"overdueloans") == 0) {
                //recupera prestiti in ritardo
                get_overdue_loans(conn, sock);
            } else if (strcmp(request_type,"overdueloansbyisbn") == 0) {
                //recupera prestiti in ritardo in base ad un isbn
                get_overdue_loans_by_isbn(conn, sock, field1);
            } 
            else {

                char *response = "Tipo di richiesta non valido.";
                send(sock, response, strlen(response), 0);
            }

    } else {
        char *response = "Richiesta non valida.";
        send(sock, response, strlen(response), 0);
    }

    // Chiudi la connessione
    close(sock);
    printf("Connessione chiusa per questo client\n");
    PQfinish(conn);
    pthread_exit(NULL);
    return NULL;
}


int start() {


    printf("Avvio del server...\n");
    printf("Server avviato e in attesa di connessioni...\n");

    //connessione al DB
    PGconn *conn = PQconnectdb("host=database port=5432 dbname=library user=postgres password=12345");
    
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Connessione al database fallita: %s\n", PQerrorMessage(conn));
        PQfinish(conn);
    } else {
        printf("Connesso\n");
    }
    //popolamento del DB
    const char *checkTablesQuery = "SELECT table_name FROM information_schema.tables WHERE table_schema='public';";
    PGresult *checkTablesResult = PQexec(conn, checkTablesQuery);
    if (PQresultStatus(checkTablesResult) != PGRES_TUPLES_OK || PQntuples(checkTablesResult) == 0) {
        printf("Le tabelle non esistono, creazione in corso...\n");

        const char *createTablesQuery = "CREATE TYPE genere AS ENUM ('classico', 'fantasy', 'storico'); CREATE TABLE users(id_utente SERIAL PRIMARY KEY, username VARCHAR(255) NOT NULL, password VARCHAR(255) NOT NULL); CREATE TABLE books (isbn VARCHAR(20) PRIMARY KEY, titolo VARCHAR(50) NOT NULL, genere GENERE NOT NULL, imageUrl VARCHAR(100) NOT NULL, autore VARCHAR(50) NOT NULL, quantita INTEGER NOT NULL, copieprestate INTEGER NOT NULL);";
        PGresult *createTablesResult = PQexec(conn, createTablesQuery);

        if (PQresultStatus(createTablesResult) != PGRES_COMMAND_OK) {
            fprintf(stderr, "Errore durante la creazione delle tabelle: %s", PQerrorMessage(conn));
            PQclear(createTablesResult);
            PQfinish(conn);
            exit(1);
        }
        PQclear(createTablesResult);

        printf("Popolazione in corso...\n");
/*const char *populateDBQuery1 = "INSERT INTO users (username, password) VALUES ('admin', 'admin');";
const char *populateDBQuery2 = "INSERT INTO books (isbn, titolo, genere, imageUrl, autore, quantita, copieprestate) VALUES "
    "('9788817154857', '1984', 'classico', 'https://covers.openlibrary.org/b/id/12054715-M.jpg', 'George Orwell', 5, 0), "
    "('9780547928227', 'The Hobbit', 'fantasy', 'https://covers.openlibrary.org/b/id/14625529-M.jpg', 'J.R.R. Tolkien', 4, 0), "
    "('9780140447934', 'War and Peace', 'classico', 'https://covers.openlibrary.org/b/id/14598041-M.jpg', 'Leo Tolstoy', 3, 0), "
    "('9780307277671', 'The Da Vinci Code', 'fantasy', 'https://covers.openlibrary.org/b/id/14553572-M.jpg', 'Dan Brown', 6, 0), "
    "('9788868360269', 'It', 'classico', 'https://covers.openlibrary.org/b/id/14656946-M.jpg', 'Stephen King', 4, 0), "
    "('9780143105428', 'Pride and Prejudice', 'classico', 'https://covers.openlibrary.org/b/id/14619629-M.jpg', 'Jane Austen', 5, 0), "
    "('9780307949486', 'The Girl with the Dragon Tattoo', 'fantasy', 'https://covers.openlibrary.org/b/id/6779579-M.jpg', 'Stieg Larsson', 5, 0), "
    "('9798392253876', 'The Great Gatsby', 'classico', 'https://covers.openlibrary.org/b/id/8248481-M.jpg', 'F. Scott Fitzgerald', 6, 0), "
    "('9788853019356', 'Treasure Island', 'storico', 'https://covers.openlibrary.org/b/id/12819540-M.jpg', 'Robert Louis Stevenson', 7, 0), "
    "('9788806228644', 'Frankenstein', 'classico', 'https://covers.openlibrary.org/b/id/12991957-M.jpg', 'Mary Shelley', 4, 0);";

                                           
    PGresult *populateDBResult1 = PQexec(conn, populateDBQuery1);
    if (PQresultStatus(populateDBResult1) != PGRES_COMMAND_OK) {
        fprintf(stderr, "Errore durante l'inserimento utenti: %s\n", PQerrorMessage(conn));
    }
    PQclear(populateDBResult1);

    PGresult *populateDBResult2 = PQexec(conn, populateDBQuery2);
    if (PQresultStatus(populateDBResult2) != PGRES_COMMAND_OK) {
        fprintf(stderr, "Errore durante l'inserimento libri: %s\n", PQerrorMessage(conn));
    }
    PQclear(populateDBResult2);

    printf("Operazioni completate con successo.\n");*/


}

    int server_fd, new_socket;
    struct sockaddr_in address;
    int addrlen = sizeof(address);

    // Creazione della socket
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) {
        perror("Socket fallita");
        exit(EXIT_FAILURE);
    }

    printf("Socket creata con successo.\n");

    // Configurazione indirizzo e porta
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(PORT);

    // Binding della socket
    if (bind(server_fd, (struct sockaddr*)&address, sizeof(address)) < 0) {
        perror("Bind fallito");
        exit(EXIT_FAILURE);
    }

    printf("Bind eseguito sulla porta %d\n", PORT);

    // Ascolto delle connessioni
    if (listen(server_fd, 10) < 0) {
        perror("Listen fallito");
        exit(EXIT_FAILURE);
    }

    printf("Server in ascolto sulla porta %d\n", PORT);


    // Ciclo per accettare più connessioni
    while (1) {
        printf("In attesa di connessioni...\n");
        if ((new_socket = accept(server_fd, (struct sockaddr*)&address, (socklen_t*)&addrlen)) < 0) {
            perror("Accept fallito");
            exit(EXIT_FAILURE);
        }

        printf("Nuova connessione accettata\n");

        // Creazione di un nuovo thread per gestire il client
        pthread_t client_thread;
        int *new_sock = malloc(sizeof(int));  // Risorsa per il nuovo socket
        *new_sock = new_socket;

        if (pthread_create(&client_thread, NULL, handle_client, (void*)new_sock) < 0) {
            perror("Impossibile creare il thread");
            free(new_sock);
            return 1;
        }

        printf("Handler assegnato al client\n");
    }
  
    close(server_fd);
    PQfinish(conn);

    return 0;
}









int main() {
    start();
    return 0;
} 