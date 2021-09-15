/*
 * Copyright 2013 http4s.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.http4s
package headers

import cats.data.NonEmptyList
import org.http4s.parser.HttpHeaderParser
import org.http4s.util.{Renderable, Writer}

// See https://tools.ietf.org/html/rfc7233

object Range extends HeaderKey.Internal[Range] with HeaderKey.Singleton {
  def apply(unit: RangeUnit, r1: SubRange, rs: SubRange*): Range =
    Range(unit, NonEmptyList.of(r1, rs: _*))

  def apply(r1: SubRange, rs: SubRange*): Range = apply(RangeUnit.Bytes, r1, rs: _*)

  def apply(begin: Long, end: Long): Range = apply(SubRange(begin, Some(end)))

  def apply(begin: Long): Range = apply(SubRange(begin, None))

  object SubRange {
    def apply(first: Long): SubRange = SubRange(first, None)
    def apply(first: Long, second: Long): SubRange = SubRange(first, Some(second))
  }

  final case class SubRange(first: Long, second: Option[Long]) extends Renderable {

    /** Base method for rendering this object efficiently */
    override def render(writer: Writer): writer.type = {
      writer << first
      second.foreach(writer << '-' << _)
      writer
    }
  }

  override def parse(s: String): ParseResult[Range] =
    HttpHeaderParser.RANGE(s)
}

final case class Range(unit: RangeUnit, ranges: NonEmptyList[Range.SubRange])
    extends Header.Parsed {
  override def key: Range.type = Range
  override def renderValue(writer: Writer): writer.type = {
    writer << unit << '=' << ranges.head
    ranges.tail.foreach(writer << ',' << _)
    writer
  }

  override def isNameValid: Boolean = true
}
