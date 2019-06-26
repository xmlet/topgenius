import React from 'react'

/**
 * It deals with stream chunks not ending on line boundaries 
 * and converting from Uint8Array to strings.
 */
async function* ndjson(reader) {
    const utf8Decoder = new TextDecoder("utf-8");
    let {value: chunk, done: readerDone} = await reader.read();
    chunk = chunk ? utf8Decoder.decode(chunk) : "";

    let re = /\n|\r|\r\n/gm;
    let startIndex = 0;

    for (;;) {
        let result = re.exec(chunk);
        if (!result) {
        if (readerDone) {
            break;
        }
        let remainder = chunk.substr(startIndex);
        ({value: chunk, done: readerDone} = await reader.read());
        chunk = remainder + (chunk ? utf8Decoder.decode(chunk) : "");
        startIndex = re.lastIndex = 0;
        continue;
        }
        const data = chunk.substring(startIndex, result.index);
        yield JSON.parse(data)
        startIndex = re.lastIndex;
    }
    if (startIndex < chunk.length) {
        // last line didn't end in a newline char
        const data = chunk.substr(startIndex)
        yield JSON.parse(data)
    }
}

class GeographicTopTracks extends React.Component {
    /**
     * Properties contains an updateFetching handler which is: (boolean) => void.
     * 
     * @param {*} props
     */
    constructor(props) {
        super(props)
        this.state = { 
            tracks: [],
            cancel: false
        }
    }

    tracksHandler(country, nrOfTracks) {
        this.setState({ tracks: [] })
        this.props.updateFetching(true)
        this.geographicTopTracks(country, nrOfTracks, 1)
    }

    cancelHandler() {
        this.setState(prevState => ({ 'tracks': prevState.tracks, 'cancel': true }))
    }

    /**
     * @param {*} country A country name, as defined by the ISO 3166-1 country names standard.
     * @param {*} nrOfTracks Number of tracks to fetch from mock of Last.fm API
     */
    lastfmUrl(country, nrOfTracks) {
        const PATH = '/api/toptracks'
        const url = `${PATH}?country=${country}&limit=${nrOfTracks}`
        return url
    }

    /**
     * @param {*} country A country name, as defined by the ISO 3166-1 country names standard.
     * @param {*} nrOfTracks The maximum number of tracks to fetch.
     */
    geographicTopTracks(country, nrOfTracks) {
        const url = this.lastfmUrl(country, nrOfTracks)
        const self = this
        this.setState({ 'tracks': [], 'cancel': false })
        fetch(url)
            .then(resp => {
                const reader = ndjson(resp.body.getReader())
                reader.next().then(function cons({value, done}) {
                    if(done || self.state.cancel) return self.props.updateFetching(false)
                    const tracks = value.tracks.track
                    self.setState(prevState => {
                        const newTracks = prevState.tracks.concat(tracks)
                        if (newTracks.length < nrOfTracks) setTimeout(() => reader.next().then(cons), 0)
                        else self.props.updateFetching(false)
                        return { 'tracks': newTracks, 'cancel': prevState.cancel }
                    })
                })
            })
            .catch(err => {
                alert(err.message)  
                self.props.updateFetching(false)  
            })
    }

    render() {
        return (
            <div>
                <p>Count: <span id="txtTracksCount">{this.state.tracks.length}</span></p>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Rank</th>
                            <th>Name</th>
                            <th>Listeners</th>
                        </tr>
                    </thead>
                    <tbody>
                        { this.state.tracks.map( (t, idx) => (
                            <tr key={idx}>
                                <td>{idx + 1}</td>
                                <td><a href={t.url} target="_blank">{t.name}</a></td>
                                <td>{t.listeners}</td>
                            </tr>
                        )) }
                    </tbody>
                </table>
            </div>
        )
    }
}

export default GeographicTopTracks
