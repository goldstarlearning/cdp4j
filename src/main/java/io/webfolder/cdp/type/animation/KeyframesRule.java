/**
 * cdp4j Commercial License
 *
 * Copyright 2017, 2019 WebFolder OÜ
 *
 * Permission  is hereby  granted,  to "____" obtaining  a  copy of  this software  and
 * associated  documentation files  (the "Software"), to deal in  the Software  without
 * restriction, including without limitation  the rights  to use, copy, modify,  merge,
 * publish, distribute  and sublicense  of the Software,  and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  IMPLIED,
 * INCLUDING  BUT NOT  LIMITED  TO THE  WARRANTIES  OF  MERCHANTABILITY, FITNESS  FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL  THE AUTHORS  OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.webfolder.cdp.type.animation;

import java.util.ArrayList;
import java.util.List;

/**
 * Keyframes Rule
 */
public class KeyframesRule {
    private String name;

    private List<KeyframeStyle> keyframes = new ArrayList<>();

    /**
     * CSS keyframed animation's name.
     */
    public String getName() {
        return name;
    }

    /**
     * CSS keyframed animation's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * List of animation keyframes.
     */
    public List<KeyframeStyle> getKeyframes() {
        return keyframes;
    }

    /**
     * List of animation keyframes.
     */
    public void setKeyframes(List<KeyframeStyle> keyframes) {
        this.keyframes = keyframes;
    }
}
