service Main{
    outputPort output{
        oneWay:
            f1(any)
        requestResponse:
            f2(any)
    }

    main{
        f1@output(void) | f2@output(void)
    }
}