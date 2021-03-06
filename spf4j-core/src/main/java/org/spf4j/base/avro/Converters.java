/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spf4j.base.avro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.spf4j.log.Slf4jLogRecord;

/**
 * @author Zoltan Farkas
 */
@ParametersAreNonnullByDefault
public final class Converters {

  private Converters() { }

  public static StackTraceElement convert(final java.lang.StackTraceElement stackTrace) {
    String className = stackTrace.getClassName();
    String fileName = stackTrace.getFileName();
    return new StackTraceElement(new Method(className, stackTrace.getMethodName()),
            fileName == null ? null :  new FileLocation(fileName, stackTrace.getLineNumber(), -1),
            org.spf4j.base.PackageInfo.getPackageInfo(className));
  }

  public static List<StackTraceElement> convert(final java.lang.StackTraceElement[] stackTraces) {
    int l = stackTraces.length;
    if (l == 0) {
      return Collections.EMPTY_LIST;
    }
    List<StackTraceElement> result = new ArrayList<>(l);
    for (java.lang.StackTraceElement st : stackTraces) {
      result.add(convert(st));
    }
    return result;
  }

  public static List<Throwable> convert(final java.lang.Throwable[] throwables) {
    int l = throwables.length;
    if (l == 0) {
      return Collections.EMPTY_LIST;
    }
    List<Throwable> result = new ArrayList<>(l);
    for (java.lang.Throwable t : throwables) {
      result.add(convert(t));
    }
    return result;
  }

  public static Throwable convert(final java.lang.Throwable throwable) {
    String message = throwable.getMessage();
    if (throwable instanceof RemoteException) {
        return new Throwable(throwable.getClass().getName(),
                message == null ? "" : message, convert(throwable.getStackTrace()),
                ((RemoteException) throwable).getRemoteCause(),
                convert(throwable.getSuppressed()));
    }
    java.lang.Throwable cause = throwable.getCause();
    return new Throwable(throwable.getClass().getName(),
            message == null ? "" : message,
            convert(throwable.getStackTrace()),
            cause == null ? null : convert(cause),
            convert(throwable.getSuppressed()));
  }

  public static RemoteException convert(final String source, final Throwable throwable) {
    return new RemoteException(source, throwable);
  }


  public static List<LogRecord> convert(final String origin, final String traceId,
          final List<Slf4jLogRecord> logRecords) {
    List<LogRecord> result = new ArrayList<>(logRecords.size());
    for (Slf4jLogRecord log : logRecords) {
      result.add(log.toLogRecord(origin, traceId));
    }
    return result;
  }

}
