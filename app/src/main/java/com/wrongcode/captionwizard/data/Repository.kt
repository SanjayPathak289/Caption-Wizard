package com.wrongcode.captionwizard.data

import javax.inject.Inject

class Repository @Inject constructor(localDataSource: LocalDataSource) {
    val local = localDataSource
}