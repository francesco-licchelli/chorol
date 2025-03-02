
service Main{
    outputPort output{
        oneWay:
            f1(any), f2(any)
    }

    main{
        spawn( i over 3 ) in result {
            f1@output(void)
        }
    }
}