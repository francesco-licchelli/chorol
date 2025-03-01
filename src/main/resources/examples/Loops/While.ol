service Main {

    outputPort output{
        oneWay:
            f1(any)
    }
    main {
        while (condition) {
            f1@output(void)
        }
    }
}
