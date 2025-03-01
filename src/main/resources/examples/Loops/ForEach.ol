service Main {

    outputPort output{
        oneWay:
            f1(any)
    }

    main {
        for (elem in array){
            f1@output(void)
        }
    }
}
