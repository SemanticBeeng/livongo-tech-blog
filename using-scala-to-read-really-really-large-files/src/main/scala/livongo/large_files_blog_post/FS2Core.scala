/* Copyright (C) 2019 Livongo Corporation - All Rights Reserved
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
package livongo.large_files_blog_post

import java.nio.file.{Files, Path}
import livongo.large_files_blog_post.common.{LineMetricsAccumulator, Result}
import cats.effect.{IO, Resource}
import fs2.Stream

object FS2Core extends FileReader {
  override def consume(path: Path): Result =
    Stream
      .resource(Resource.fromAutoCloseable(IO(Files.newBufferedReader(path))))
      .flatMap {
        Stream.unfold(_) { reader =>
          Option(reader.readLine()).map(_ -> reader)
        }
      }
      .compile
      .fold(LineMetricsAccumulator.empty)(_ addLine _)
      .map(_.asResult)
      .unsafeRunSync()

  override def description: String = "fs2-core"
}
