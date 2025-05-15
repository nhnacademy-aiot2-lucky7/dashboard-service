package com.nhnacademy.dashboard.exception;

import com.nhnacademy.common.exception.CommonHttpException;

public class AlreadyFolderNameException extends CommonHttpException {
  private static final int STATUS_CODE = 400;

  public AlreadyFolderNameException(String message) {
    super(STATUS_CODE, message);
  }
}
