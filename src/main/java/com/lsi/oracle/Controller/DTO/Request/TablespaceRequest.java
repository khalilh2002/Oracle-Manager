package com.lsi.oracle.Controller.DTO.Request;

public record TablespaceRequest(
  String name ,
  String tablespace_name,
  String role_name,
  String datafile_path,
  Long size
) {}
