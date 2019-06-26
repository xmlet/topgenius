import React from 'react'
import ReactDOM from 'react-dom'
import ReactLoading from 'react-loading'
import InputCountry from './InputCountry'
import GeographicTopTracks from './GeographicTopTracks'

class App extends React.Component {
    constructor(props) {
        super(props)
        this.state = { 'fetching': false }
        this.geoTopTracks = React.createRef();
    }

    tracksHandler(country, limit) {
        this.geoTopTracks.current.tracksHandler(country, limit)
    }

    cancelHandler() {
        this.geoTopTracks.current.cancelHandler()
    }

    render() {
        return (
            <div>
                <div class="jumbotron">
                    <p class="lead">TopGenius.eu</p>
                    <hr/>
                    <InputCountry 
                        tracksHandler={(country, limit) => this.tracksHandler(country, limit)}
                        cancelHandler={() => this.cancelHandler()}
                    />
                    <div>
                        {this.state.fetching && <ReactLoading type={"bars"} color="#666666" /> }
                    </div>
                </div>
                <br />
                <GeographicTopTracks 
                    ref={this.geoTopTracks}
                    updateFetching={(fetching => this.setState({ 'fetching': fetching }))} 
                />
            </div>
        )
    }
}

// ========================================

ReactDOM.render(
    <App />,
    document.getElementById('root')
);

