service Main{
    outputPort output{
        oneWay:
            f1(any), f2(any)
    }

    main{
        f1@output(void) | f2@output(void)
    }
}