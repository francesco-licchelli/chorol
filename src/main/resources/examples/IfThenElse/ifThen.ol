service Main {

    outputPort output{
        oneWay:
            f1(any)
    }


    main {
        if ( condizione ){
            f1@output(void)
        }
    }
}
