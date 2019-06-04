import React from 'react'

class GeographicTopTracks extends React.Component {
    constructor(props) {
        super(props)
        this.state = { tracks: [] }
    }

    tracksHandler(country, limit) {
        this.setState({ tracks: [] })
        this.props.updateFetching(true)
        this.geographicTopTracks(country, limit, 1)
    }
    geographicTopTracks(country, limit, page) {
        const RESULTS = 50; // number of results to fetch per page
        page = page ? page : 1
        fetch(lastfmUrl(country, page, RESULTS))
            .then(resp => resp.json())
            .then(data => {
                if(!data.tracks) throw Error('No tracks for given country!')
                return data.tracks.track
            })
            .then(tracks => this.updateState(tracks, country, limit, page))
            .catch(err => {
                this.props.updateFetching(false)
                alert(err.message)
            })
    }
    /**
     * There is a BUG in LastFM Web API that returns more results than those
     * specified in the limit query parameter.
     * Sometimes the page 2 returns 100 rather than 50 records for a limit of 50.
     * 
     * @param {*} tracks New tracks to concatenate on state.
     * @param {*} country A country name, as defined by the ISO 3166-1 country names standard.
     * @param {*} page The page number.
     * @param {*} results The number of results to fetch per page.
     */
    updateState(tracks, country, limit, page) {
        if(tracks.length === 0) 
            return
        const maxSize = limit - this.state.tracks.length
        this.props.updateFetching(true)
        if(tracks.length > maxSize) { // base case
            tracks = tracks.slice(0, maxSize)
            this.props.updateFetching(false)
        } else {
            const newTracks = this.state.tracks.concat(tracks)
            this.setState({ 'tracks': newTracks })
            this.geographicTopTracks(country, limit, page + 1)
        }
    }
    render() {
        return (
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
        )
    }
}

export default GeographicTopTracks

// ========================================

/**
 * @param {*} country A country name, as defined by the ISO 3166-1 country names standard.
 * @param {*} page The page number.
 * @param {*} results The number of results to fetch per page.
 */
function lastfmUrl(country, page, results) {
    const API_KEY = '038cde478fb0eff567330587e8e981a4'
    const HOST = 'http://ws.audioscrobbler.com/2.0/'
    const path = `${HOST}?method=geo.gettoptracks&country=${country}&page=${page}&limit=${results}&format=json&api_key=${API_KEY}`
    return path
}
