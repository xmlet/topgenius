import React from 'react'
import { EventEmitter } from 'events'

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
     * Properties contains an updateFetching handle,r which is: (boolean) => void,
     * and an env string with: "production" or "mock".
     * @param {*} props
     */
    constructor(props) {
        super(props)
        this.state = { tracks: [] }
    }

    tracksHandler(country, nrOfTracks) {
        this.setState({ tracks: [] })
        this.props.updateFetching(true)
        if(this.props.env === 'production')
            this.geographicTopTracks(country, nrOfTracks, 1)
        else 
            this.mockGeographicTopTracks(country, nrOfTracks)
    }

    /**
     * There is a BUG in LastFM Web API that sometimes returns more results than those
     * specified in the limit query parameter.
     * Sometimes the page 2 returns 100 rather than 50 records for a limit of 50.
     * 
     * @param {*} country A country name, as defined by the ISO 3166-1 country names standard.
     * @param {*} page The page number.
     */
    lastfmUrl(country, page) {
        // RESULTS is the number of tracks to fetch per page
        const RESULTS = 50
        const API_KEY = '038cde478fb0eff567330587e8e981a4'
        const HOST = 'http://ws.audioscrobbler.com/2.0/'
        const path = `${HOST}?method=geo.gettoptracks&country=${country}&page=${page}&limit=${RESULTS}&format=json&api_key=${API_KEY}`
        return path
    }
    /**
     * @param {*} country A country name, as defined by the ISO 3166-1 country names standard.
     * @param {*} nrOfTracks Number of tracks to fetch from mock of Last.fm API
     */
    mockLastfmUrl(country, nrOfTracks) {
        const PAGE = -1 // backend should send all tracks in limit, which is equals to nrOfTracks
        const API_KEY = '038cde478fb0eff567330587e8e981a4'
        const HOST = '/lastfmmock'
        const path = `${HOST}?method=geo.gettoptracks&country=${country}&page=${PAGE}&limit=${nrOfTracks}&format=json&api_key=${API_KEY}`
        return path
    }

    /**
     * @param {*} country A country name, as defined by the ISO 3166-1 country names standard.
     * @param {*} nrOfTracks The maximum number of tracks to fetch.
     * @param {*} page The page number.
     */
    async mockGeographicTopTracks(country, nrOfTracks) {
        try {
            const url = this.mockLastfmUrl(country, nrOfTracks)
            const resp = await fetch(url)
            for await (let obj of ndjson(resp.body.getReader())) {
                const tracks = obj.tracks.track
                this.setState(prevState => ({ 'tracks': prevState.tracks.concat(tracks) }))
            }
        } catch(err) {
            alert(err.message)    
        } finally {
            this.props.updateFetching(false)
        }
    }

    /**
     * @param {*} country A country name, as defined by the ISO 3166-1 country names standard.
     * @param {*} nrOfTracks The maximum number of tracks to fetch.
     * @param {*} page The page number.
     */
    geographicTopTracks(country, nrOfTracks, page) {
        page = page ? page : 1
        const url = this.lastfmUrl(country, page)
        fetch(url)
            .then(resp => resp.json())
            .then(data => {
                if(!data.tracks) throw Error('No tracks for given country!')
                return data.tracks.track
            })
            .then(tracks => this.updateState(tracks, country, nrOfTracks, page))
            .catch(err => {
                this.props.updateFetching(false)
                alert(err.message)
            })
    }
    /**
     * @param {*} tracks New tracks to concatenate on state.
     * @param {*} country A country name, as defined by the ISO 3166-1 country names standard.
     * @param {*} page The page number.
     */
    updateState(tracks, country, nrOfTracks, page) {
        if(tracks.length === 0) 
            return this.props.updateFetching(false)
        const maxSize = nrOfTracks - this.state.tracks.length
        this.props.updateFetching(true)
        if(tracks.length > maxSize) { // base case
            tracks = tracks.slice(0, maxSize)
            this.props.updateFetching(false)
        } else {
            const newTracks = this.state.tracks.concat(tracks)
            this.setState({ 'tracks': newTracks })
            this.geographicTopTracks(country, nrOfTracks, page + 1)
        }
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
